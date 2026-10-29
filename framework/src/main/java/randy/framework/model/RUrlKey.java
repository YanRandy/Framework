package randy.framework.model;

import java.util.Objects;

/**
 * Clé de la table des routes : URL + verbe HTTP.
 * equals/hashCode sont redéfinis pour que deux clés identiques tombent sur la même entrée de la HashMap.
 * Le verbe est mis en majuscules pour que "get" et "GET" soient équivalents.
 */
public class RUrlKey {
    public String url;
    public String httpMethod;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public RUrlKey(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod.toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        RUrlKey urlKey = (RUrlKey) obj;
        return url.equals(urlKey.url) && httpMethod.equals(urlKey.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }
}
