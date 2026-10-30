package com.ocsin.jaya.graphql;

import com.ocsin.jaya.application.DiagnosticsService;
import com.ocsin.jaya.application.InputGuard;
import com.ocsin.jaya.application.NoopExternalPublisher;
import com.ocsin.jaya.application.perf.CaffeineCacheStore;
import com.ocsin.jaya.application.perf.OperationMonitor;
import com.ocsin.jaya.application.testing.Fixtures;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.security.AuthorizationPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQLProviderTest {

    @Test
    @DisplayName("une requête trop coûteuse est refusée et une lecture anonyme reste possible")
    void limitsComplexityAndHidesDrafts() {
        Fixtures fixtures = Fixtures.create();
        var draft = fixtures.service.save(Fixtures.editor(), new com.ocsin.jaya.application.SaveArticle(
                null, "GraphQL", null, "Schéma borné", "fr", java.util.List.of("api")));
        fixtures.service.submit(Fixtures.editor(), draft.id());
        fixtures.service.publish(Fixtures.publisher(), draft.id());

        OperationMonitor monitor = new OperationMonitor(Duration.ofHours(1), 10, fixtures.clock);
        GraphQLProvider graphql = new GraphQLProvider(
                fixtures.service,
                new DiagnosticsService(new AuthorizationPolicy(), new CaffeineCacheStore(Duration.ofMinutes(1), 10), monitor),
                monitor,
                40,
                8);

        Map<String, Object> found = graphql.execute(
                "query { search(term: \"graphql\") { total items { title status } } }",
                null,
                Map.of(),
                Principal.anonymous());
        assertThat(found.get("errors")).isNull();
        assertThat(found.get("data").toString()).contains("GraphQL");

        StringBuilder fat = new StringBuilder("query {");
        for (int i = 0; i < 30; i++) {
            fat.append(" a").append(i).append(": search { total items { id title } }");
        }
        fat.append(" }");
        Map<String, Object> rejected = graphql.execute(fat.toString(), null, Map.of(), Principal.anonymous());
        assertThat(rejected.get("errors")).isNotNull();
    }
}
