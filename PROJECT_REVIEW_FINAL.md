# Proje Yeniden Tarama Raporu

## Kapsam

Proje; Java kaynakları, controller, service, repository, entity, DTO, security, configuration, Docker dosyaları, README, API test scriptleri ve mevcut test yapısı üzerinden yeniden incelenmiştir.

Değerlendirme düşük trafikli bir öğrenci projesi varsayımıyla yapılmıştır. Redis, Kafka, outbox pattern, dağıtık lock ve yüksek trafik odaklı altyapılar bu değerlendirmede gerekli kabul edilmemiştir.

Migration ve kapsamlı test eksiklikleri hata olarak değerlendirilmemiştir.

## Genel Sonuç

Yapılan altı değişikliğin büyük bölümü doğru uygulanmıştır. Proje temiz şekilde derlenmekte, Spring context başarıyla açılmakta ve mevcut test geçmektedir.

Projenin mevcut mimarisi düşük trafikli bir öğrenci projesi için yeterlidir. Kalan konular daha çok küçük davranış tutarsızlıkları, test scripti uyumsuzlukları ve dokümantasyon iyileştirmeleridir.

## Doğrulanan Değişiklikler

### 1. Docker ve README çalıştırma akışı

[README.md](README.md) içinde iki farklı çalışma modu ayrılmıştır:

- Tam Docker: `docker compose up --build`
- Yerel uygulama ve Docker PostgreSQL: `docker compose up -d postgres` ardından `./mvnw spring-boot:run`

Swagger adresi dokümanlarda standartlaştırılmıştır:

```text
http://localhost:8080/swagger-ui/index.html
```

Bu değişiklik başarılıdır.

Küçük not: README içindeki `cp .env.example .env` komutu Windows PowerShell’de doğrudan çalışmayabilir. Windows için ayrıca şu komut eklenebilir:

```powershell
Copy-Item .env.example .env
```

### 2. Hata cevaplarının standartlaştırılması

[ErrorResponse.java](src/main/java/com/example/ticketsystem/exception/ErrorResponse.java), [BusinessException.java](src/main/java/com/example/ticketsystem/exception/BusinessException.java) ve [GlobalExceptionHandler.java](src/main/java/com/example/ticketsystem/exception/GlobalExceptionHandler.java) güncellenmiştir.

Hata cevaplarına `code` alanı eklenmiş ve validation detayları desteklenmiştir. Kullanılan hata kodları genel olarak doğru seçilmiştir:

- `VALIDATION_ERROR`
- `EVENT_SOLD_OUT`
- `EVENT_NOT_ACTIVE`
- `EVENT_CANCELLED`
- `EVENT_PASSED`
- `TICKET_ALREADY_CANCELLED`
- `INVALID_REFRESH_TOKEN`
- `TOKEN_REUSE_DETECTED`
- `DATA_INTEGRITY_VIOLATION`
- `INTERNAL_SERVER_ERROR`

Bazı `BusinessException` kullanımları hâlâ hata kodu vermeden oluşturuluyor ve `BUSINESS_ERROR` kodunu kullanıyor. Bu kritik değildir; ileride tüm iş kuralları özel kodlarla standardize edilebilir.

### 3. Koltuk benzersizliği

[Ticket.java](src/main/java/com/example/ticketsystem/entity/Ticket.java) içinde `event_id` ve `seat_number` için şu veritabanı constraint’i tanımlanmıştır:

```java
@UniqueConstraint(
    name = "uk_ticket_event_seat",
    columnNames = {"event_id", "seat_number"}
)
```

Kullanıcı bileti iptal ettiğinde koltuk numarasının sonuna `-CANCELLED-{id}` eklenmektedir. Böylece gerçek koltuk yeni satın alımlarda tekrar kullanılabilmektedir.

Bu yaklaşım düşük trafikli proje için uygundur.

### 4. Event PUT/PATCH validation

[EventUpdateRequest.java](src/main/java/com/example/ticketsystem/dto/EventUpdateRequest.java) ve [EventServiceImpl.java](src/main/java/com/example/ticketsystem/service/EventServiceImpl.java) içinde validation kuralları sıkılaştırılmıştır.

- PATCH isteğinde boş veya yalnızca boşluklardan oluşan event adı engellenmektedir.
- PUT isteğinde gerekli alanlar zorunlu tutulmaktadır.
- Etkinlik tarihi ve kapasite kuralları service katmanında kontrol edilmektedir.

PUT ve PATCH ayrımı mevcut haliyle mantıklıdır.

### 5. JWT secret fail-fast kontrolü

[JwtService.java](src/main/java/com/example/ticketsystem/security/JwtService.java) uygulama başlarken JWT secret değerini kontrol etmektedir:

- Değer boş olmamalıdır.
- Base64 formatında olmalıdır.
- En az 32 byte, yani 256 bit olmalıdır.

Güvensiz veya eksik secret durumunda uygulamanın başlamaması doğru bir davranıştır.

### 6. Admin parolasının parametreleştirilmesi

[test_api.ps1](test_api.ps1) ve [test_all_apis.ps1](test_all_apis.ps1) artık `AdminEmail` ve `AdminPassword` parametrelerini ve environment variable kullanımını desteklemektedir.

Ancak scriptlerde hâlâ şu fallback bulunmaktadır:

```powershell
if (-not $AdminPassword) { $AdminPassword = "ChangeMe123!" }
```

Bu nedenle parametre veya environment variable verilmediğinde sabit parola kullanılmaya devam etmektedir. Bu, düzeltilmesi gereken küçük bir güvenlik ve kullanım problemidir.

## Kalan Sorunlar ve Öneriler

### 1. API test scriptinde kapasite çelişkisi

**Öncelik: Orta**

[test_all_apis.ps1](test_all_apis.ps1) içinde event 50 koltukla oluşturuluyor:

```powershell
totalSeats=50
```

Daha sonra PUT isteğinde kapasite 100’e çıkarılıyor:

```powershell
totalSeats=100
```

Ancak [EventServiceImpl.java](src/main/java/com/example/ticketsystem/service/EventServiceImpl.java) kapasitenin sonradan artırılmasına izin vermiyor.

Önerilen çözüm:

- Test scriptinde PUT kapasitesini 50 veya daha düşük yapın.
- Ya da test event’ini baştan 100 koltukla oluşturun.
- Kapasite artışını engelleyen iş kuralını değiştirmeyin; mevcut kural veri tutarlılığı açısından daha güvenlidir.

### 2. Event iptalinde ticket koltuk alanı güncellenmiyor

**Öncelik: Orta**

Kullanıcı bileti iptal ettiğinde [TicketServiceImpl.java](src/main/java/com/example/ticketsystem/service/TicketServiceImpl.java) koltuk numarası değiştirilmektedir.

Ancak admin bir event’i iptal ettiğinde [EventServiceImpl.java](src/main/java/com/example/ticketsystem/service/EventServiceImpl.java) ticket status’i `CANCELLED` yapılmakta, koltuk numarasına suffix eklenmemektedir.

Event tekrar aktif edilemediği için hemen bir çakışma oluşmayabilir. Yine de iki iptal akışının aynı davranışı göstermesi daha doğrudur.

Önerilen davranış:

```text
KOLTUK-1 -> KOLTUK-1-CANCELLED-{ticketId}
```

### 3. Logout endpoint davranışı netleştirilmeli

**Öncelik: Düşük/Orta**

[SecurityConfig.java](src/main/java/com/example/ticketsystem/security/SecurityConfig.java) içinde logout endpoint’i `permitAll` olarak tanımlanmıştır.

Buna rağmen [AuthServiceImpl.java](src/main/java/com/example/ticketsystem/service/AuthServiceImpl.java) logout işlemi için access token zorunlu tutmaktadır.

Token olmadan yapılan logout isteği bu nedenle standart `401` yerine business hatası olarak `400` dönebilir.

Önerilen çözüm:

- Logout endpoint’ini authenticated yapmak.
- Ya da token yokken logout çağrısını güvenli ve tutarlı şekilde başarılı kabul etmek.

### 4. Veritabanı constraint’i fiziksel olarak kontrol edilmeli

Temiz test sırasında Hibernate şu uyarıyı üretmiştir:

```text
constraint "uk_ticket_event_seat" of relation "tickets" does not exist, skipping
```

Bu uyarı mevcut geliştirme veritabanında eski constraint bulunmadığı için oluşmuş olabilir. Uygulama başarıyla başlamıştır.

Entity tanımı doğru olsa da PostgreSQL’de constraint’in gerçekten mevcut olduğu bir kez kontrol edilmelidir. Bu, migration talebi değildir; mevcut veritabanı şemasının güncel olup olmadığını doğrulama adımıdır.

### 5. Windows README komutu iyileştirilebilir

README’deki şu komut Linux/macOS için uygundur:

```bash
cp .env.example .env
```

Windows kullanıcıları için şu alternatif eklenebilir:

```powershell
Copy-Item .env.example .env
```

### 6. `.env.example` yalnızca geliştirme değerlerini belirtmeli

[.env.example](.env.example) içinde örnek admin ve database parolaları bulunması normaldir. Ancak README’de bu değerlerin yalnızca local geliştirme için olduğu ve production’da mutlaka değiştirilmesi gerektiği açıkça belirtilmelidir.

`.env` dosyasının [.gitignore](.gitignore) içinde olması olumlu bir uygulamadır.

## Şimdilik Gerekli Olmayan Konular

Düşük trafik ve tek uygulama instance’ı varsayımı altında aşağıdaki konular şu aşamada gerekli değildir:

- Redis tabanlı rate limiter
- Kafka veya mesaj kuyruğu
- Outbox pattern
- Dağıtık scheduled job lock
- Çoklu instance koordinasyonu
- Büyük ölçekli event-driven notification sistemi
- İleri seviye cache sistemi
- Zorunlu pagination dönüşümü

Mevcut `ConcurrentHashMap` tabanlı rate limiter ve scheduled job yapısı bu proje ölçeği için yeterlidir.

## Doğrulama Sonucu

Son çalıştırılan komut:

```powershell
./mvnw.cmd clean test
```

Sonuç:

- 77 Java kaynak dosyası derlendi.
- Spring context başarıyla açıldı.
- 1 test çalıştı.
- 0 failure.
- 0 error.
- Build başarılı.
- Editör/Java diagnostic hatası bulunmadı.

Mevcut test yalnızca Spring context yüklenmesini doğrulamaktadır. Kullanıcının belirttiği gibi kapsamlı testler daha sonra ekleneceği için bu durum eksiklik olarak değerlendirilmemiştir.

## Önerilen Son İş Sırası

1. [test_all_apis.ps1](test_all_apis.ps1) içindeki 50’den 100’e kapasite artırma çelişkisini düzeltin.
2. Admin parola fallback’ini kaldırın veya parola parametresini zorunlu yapın.
3. Event iptalinde cancelled ticket koltuk numarasını da güncelleyin.
4. Logout endpoint davranışını netleştirin.
5. PostgreSQL’de `uk_ticket_event_seat` constraint’inin varlığını kontrol edin.
6. README’ye Windows `.env` kopyalama komutunu ekleyin.

## Sonuç

Yapılan değişiklikler genel olarak başarılıdır. Proje şu anda düşük trafikli bir öğrenci projesi için yeterli seviyededir.

Kalan problemler büyük bir mimari değişiklik gerektirmemektedir. Redis, Kafka veya outbox gibi altyapılara ihtiyaç yoktur. En faydalı son dokunuşlar test scriptinin gerçek iş kurallarıyla uyumlu hale getirilmesi, iki ticket iptal akışının birleştirilmesi ve admin parola fallback’inin kaldırılmasıdır.
