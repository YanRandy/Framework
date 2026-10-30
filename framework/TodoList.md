Sprint 10:
annotation: `RAuthenticated.java` sur les methodes et les types
classe des metiers et fonctionnalites
package: `randy.framework.auth`
type de metier:
- `allow`
- `deny`

Ajout de context param:
- `userKey`
- `loginUrl`

Sprint 10-bis:
annotation: ajout de `String[] roles default {}`

type de metier:
- `hasAnyRole`
- ...

Ajout de context param:
- `rolekey`

Reste ?

Tests:
``` bash
B=http://localhost:8080/code-test
curl -s -w "\nHTTP %{http_code}\n" $B/sprint/auth/public
curl -s -w "\nHTTP %{http_code}\n" $B/sprint/auth/profil
curl -s -o /dev/null -w "HTTP %{http_code} -> %{redirect_url}\n" $B/sprint/auth/page
curl -s -c c.txt -b c.txt -d "username=Rakoto&password=1234&role=USER" $B/sprint/auth/login
curl -s -b c.txt -w "\nHTTP %{http_code}\n" $B/sprint/auth/profil
curl -s -b c.txt -w "\nHTTP %{http_code}\n" $B/sprint/auth/admin
curl -s -c c.txt -b c.txt -d "username=Rakoto&password=1234&role=MANAGER" $B/sprint/auth/login
curl -s -b c.txt -w "\nHTTP %{http_code}\n" $B/sprint/auth/staff
curl -s -b c.txt -w "\nHTTP %{http_code}\n" $B/sprint/auth/admin
curl -s -c c.txt -b c.txt -d "username=Rakoto&password=1234&role=ADMIN" $B/sprint/auth/login
curl -s -b c.txt -w "\nHTTP %{http_code}\n" $B/sprint/auth/admin
curl -s -b c.txt -X POST $B/sprint/auth/logout
curl -s -b c.txt -w "\nHTTP %{http_code}\n" $B/sprint/auth/profil
```

