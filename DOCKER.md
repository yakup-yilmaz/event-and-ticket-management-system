# Docker ile Çalıştırma

## Gereksinimler

- Docker Desktop
- Docker Desktop Linux containers modu

## Başlatma

1. `.env.example` dosyasını `.env` olarak kopyalayın.
2. `.env` içindeki DB ve admin bilgilerini düzenleyin.
3. Docker Desktop'ın açık ve Linux engine'in çalışır olduğundan emin olun.
4. Proje kök dizininde çalıştırın:

```bash
docker compose up --build
```

Uygulama:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

PostgreSQL:

```text
localhost:5432/ticketSystem
```

## Durdurma

```bash
docker compose down
```

## Veritabanını Sıfırlama

Development verilerini ve PostgreSQL volume'unu silmek için:

```bash
docker compose down -v
```

Sonraki `docker compose up --build` çalıştırmasında veritabanı boş olarak yeniden oluşturulur ve Hibernate entity'lere göre tabloları kurar.

## Notlar

- `.env` dosyası Git'e gönderilmemelidir.
- GitHub'a `.env.example` gönderilmelidir.
- Uygulama PostgreSQL'e Docker network içinden `postgres:5432` adresiyle bağlanır.
- Local NetBeans çalıştırmasında `.env` dosyası Spring Boot tarafından optional config import ile okunur.
