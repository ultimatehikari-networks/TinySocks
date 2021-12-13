## Tiny SOCKS5 Proxy

Build & run:

```
$ mvn verify assembly:single
$ java -jar ./target/TinySocks-1.0-SNAPSHOT.jar
```

Tests:

```
$curl --socks5-hostname localhost:8080 https://mirror.yandex.ru/archlinux/community/os/x86_64/zathura-pdf-mupdf-0.3.7-1-x86_64.pkg.tar.zst > /dev/null
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 21.2M  100 21.2M    0     0  1065k      0  0:00:20  0:00:20 --:--:-- 1004k

$curl https://mirror.yandex.ru/archlinux/community/os/x86_64/zathura-pdf-mupdf-0.3.7-1-x86_64.pkg.tar.zst > /dev/null 
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 21.2M  100 21.2M    0     0  1091k      0  0:00:19  0:00:19 --:--:-- 1137k

```

![tiny socks](http://fit.ippolitov.me/CN_2/2021/socks.jpg)
