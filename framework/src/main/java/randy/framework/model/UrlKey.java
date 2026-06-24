package randy.framework.model;

import java.util.Objects;

public class UrlKey {
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

    public UrlKey(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod.toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UrlKey urlKey = (UrlKey) obj;
        return url.equals(urlKey.url) && httpMethod.equals(urlKey.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }
}
