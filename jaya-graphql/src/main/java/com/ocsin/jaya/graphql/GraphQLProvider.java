package com.ocsin.jaya.graphql;

import com.ocsin.jaya.application.ContentAccessException;
import com.ocsin.jaya.application.DiagnosticsService;
import com.ocsin.jaya.application.EditorialService;
import com.ocsin.jaya.application.SaveArticle;
import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.application.perf.OperationMonitor;
import com.ocsin.jaya.application.view.ArticleView;
import com.ocsin.jaya.application.view.SearchView;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.DomainException;
import com.ocsin.jaya.domain.NotFoundException;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.ValidationException;
import graphql.ErrorType;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Schéma GraphQL borné en profondeur et en complexité.
 */
public final class GraphQLProvider {

    private static final Logger log = LoggerFactory.getLogger(GraphQLProvider.class);

    private final GraphQL graphQL;
    private final OperationMonitor monitor;

    public GraphQLProvider(EditorialService editorial,
                           DiagnosticsService diagnostics,
                           OperationMonitor monitor,
                           int maxComplexity,
                           int maxDepth) {
        this.monitor = monitor;
        this.graphQL = GraphQL.newGraphQL(schema(editorial, diagnostics))
                .defaultDataFetcherExceptionHandler(new SafeErrors())
                .instrumentation(new ChainedInstrumentation(List.of(
                        new MaxQueryDepthInstrumentation(maxDepth),
                        new MaxQueryComplexityInstrumentation(maxComplexity))))
                .build();
    }

    public Map<String, Object> execute(String query, String operationName, Map<String, Object> variables, Principal principal) {
        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables == null ? Map.of() : variables)
                .graphQLContext(Map.of("principal", principal == null ? Principal.anonymous() : principal))
                .build();
        ExecutionResult result = monitor.time("graphql.execute", () -> graphQL.execute(input));
        return result.toSpecification();
    }

    private static GraphQLSchema schema(EditorialService editorial, DiagnosticsService diagnostics) {
        TypeDefinitionRegistry registry = new SchemaParser().parse(loadSchema());
        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", type -> type
                        .dataFetcher("article", env -> {
                            try {
                                return ArticleView.of(editorial.find(principal(env), ArticleId.parse(env.getArgument("id"))));
                            } catch (NotFoundException ex) {
                                return null;
                            }
                        })
                        .dataFetcher("search", env -> SearchView.of(editorial.search(principal(env), new SearchCriteria(
                                env.getArgument("term"),
                                status(env.getArgument("status")),
                                env.getArgument("locale"),
                                number(env.getArgument("limit"), 20),
                                number(env.getArgument("offset"), 0)))))
                        .dataFetcher("diagnostics", env -> diagnostics.snapshot(principal(env))))
                .type("Mutation", type -> type
                        .dataFetcher("saveArticle", env -> ArticleView.of(editorial.save(principal(env), command(env.getArgument("input")))))
                        .dataFetcher("submitArticle", env -> ArticleView.of(editorial.submit(principal(env), ArticleId.parse(env.getArgument("id")))))
                        .dataFetcher("publishArticle", env -> ArticleView.of(editorial.publish(principal(env), ArticleId.parse(env.getArgument("id")))))
                        .dataFetcher("rejectArticle", env -> ArticleView.of(editorial.reject(principal(env), ArticleId.parse(env.getArgument("id")))))
                        .dataFetcher("syncArticle", env -> ArticleView.of(editorial.sync(principal(env), ArticleId.parse(env.getArgument("id"))))))
                .build();
        return new SchemaGenerator().makeExecutableSchema(registry, wiring);
    }

    private static Principal principal(graphql.schema.DataFetchingEnvironment env) {
        Principal principal = env.getGraphQlContext().get("principal");
        return principal == null ? Principal.anonymous() : principal;
    }

    @SuppressWarnings("unchecked")
    private static SaveArticle command(Object raw) {
        if (!(raw instanceof Map<?, ?> input)) {
            throw new ValidationException("La mutation est incomplète");
        }
        Object id = input.get("id");
        return new SaveArticle(
                id == null ? null : ArticleId.parse(id.toString()),
                text(input.get("title")),
                text(input.get("summary")),
                text(input.get("body")),
                text(input.get("locale")),
                tags(input.get("tags")));
    }

    private static PublicationStatus status(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return PublicationStatus.valueOf(raw.toString().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Statut inconnu");
        }
    }

    private static int number(Object raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(raw.toString());
    }

    private static String text(Object raw) {
        return raw == null ? null : raw.toString();
    }

    private static List<String> tags(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new ValidationException("Les tags doivent être une liste");
        }
        return list.stream().map(item -> item == null ? "" : item.toString()).toList();
    }

    private static String loadSchema() {
        try (InputStream in = GraphQLProvider.class.getClassLoader().getResourceAsStream("graphql/schema.graphqls")) {
            if (in == null) {
                throw new ContentAccessException("Schéma GraphQL introuvable");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static final class SafeErrors implements DataFetcherExceptionHandler {

        @Override
        public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters parameters) {
            Throwable error = unwrap(parameters.getException());
            String message = error instanceof DomainException ? error.getMessage() : "Erreur interne";
            String code = error instanceof DomainException domain ? domain.code() : "internal";
            if (!(error instanceof DomainException)) {
                log.error("Erreur GraphQL", error);
            }
            GraphQLError graphQLError = GraphqlErrorBuilder.newError()
                    .message(message)
                    .path(parameters.getPath())
                    .location(parameters.getSourceLocation())
                    .errorType(ErrorType.DataFetchingException)
                    .extensions(Map.of("code", code))
                    .build();
            return CompletableFuture.completedFuture(DataFetcherExceptionHandlerResult.newResult()
                    .error(graphQLError)
                    .build());
        }

        private static Throwable unwrap(Throwable error) {
            Throwable current = error;
            while (current.getCause() != null && current.getClass().getName().contains("CompletionException")) {
                current = current.getCause();
            }
            return current;
        }
    }
}
