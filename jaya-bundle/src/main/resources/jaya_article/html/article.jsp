<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="jaya:title" var="headline"/>
<jcr:nodeProperty node="${currentNode}" name="jaya:summary" var="summary"/>
<article class="jaya-article">
  <h1><c:out value="${headline.string}"/></h1>
  <p><c:out value="${summary.string}"/></p>
</article>
