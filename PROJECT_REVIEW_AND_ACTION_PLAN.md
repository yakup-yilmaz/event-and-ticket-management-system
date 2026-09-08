# Proje Değerlendirmesi ve Aksiyon Planı

Bu doküman, Event and Ticket Management System projesinin düşük ve orta düzey trafik altında değerlendirilmesi sonucunda hazırlanmıştır.

Redis, Kafka, outbox pattern, dağıtık lock veya yüksek trafik odaklı karmaşık altyapılar bu planın kapsamı dışındadır. Projenin mevcut öğrenci projesi hedefi için daha önemli olan konular; veri doğruluğu, güvenlik ayarları, çalıştırma kolaylığı ve kullanıcı davranışlarının doğru yönetilmesidir.

## Genel Değerlendirme

Proje; Spring Boot katmanlı mimarisi, JWT güvenliği, refresh token kullanımı, DTO/mapper ayrımı, strategy pattern, transaction yönetimi ve pessimistic lock yaklaşımıyla güçlü bir temel oluşturuyor.

Mevcut yapı küçük ölçekli bir sistem için yeterlidir. Aşağıdaki maddeler tamamlandığında proje daha tutarlı, daha güvenli ve sunuma daha hazır hale gelecektir.

## Önceliklendirilmiş Sorunlar

### 1. Docker ve README çalıştırma akışı çelişkili

**Öncelik:** Yüksek

README içinde Docker uygulaması başlatıldıktan sonra ayrıca Maven ile uygulamanın çalıştırılması öneriliyor. Docker Compose zaten uygulamayı `8080` portunda başlattığı için bu iki komut birlikte kullanıldığında port çakışması oluşabilir.

**Önerilen çözüm:**

- Docker kullanımı için tek akış belirleyin:

```bash
docker compose up --build
```

- Yerel Maven çalıştırma adımlarını Docker akışından ayrı gösterin.
- Swagger adresini dokümanların tamamında aynı kullanın:

```text
http://localhost:8080/swagger-ui/index.html
```

İlgili dosyalar: [README.md](README.md), [DOCKER.md](DOCKER.md), [docker-compose.yml](docker-compose.yml)

### 2. API test scriptlerindeki admin parolası sabit ve tutarsız

**Öncelik:** Yüksek

[test_api.ps1](test_api.ps1) ve [test_all_apis.ps1](test_all_apis.ps1) içinde `ChangeMe123!` sabit parolası kullanılıyor. [.env.example](.env.example) içinde ise `change-me` bulunuyor.

**Önerilen çözüm:**

- Script içine sabit parola yazmayın.
- Parolayı script parametresi veya environment variable olarak alın.
- En azından `.env.example`, README ve scriptlerde aynı geliştirme değeri kullanılsın.
- Gerçek parolalar Git’e gönderilmesin.

### 3. Koltuk benzersizliği yalnızca servis koduna bırakılmış

**Öncelik:** Yüksek

Bilet satın alma sırasında pessimistic lock kullanılması olumlu bir yaklaşımdır. Ancak `event_id` ve `seat_number` birlikte veritabanında unique olarak tanımlı değilse veri tutarlılığı yalnızca servis akışına bağlı kalır.

**Önerilen çözüm:**

- `Ticket` entity’sinde event ve koltuk numarasına yönelik unique constraint tanımlayın.
- İptal edilmiş biletler için constraint davranışını ayrıca belirleyin.
- Constraint ihlalini uygun bir business error response’a dönüştürün.

İlgili dosyalar: [Ticket.java](src/main/java/com/example/ticketsystem/entity/Ticket.java), [TicketServiceImpl.java](src/main/java/com/example/ticketsystem/service/TicketServiceImpl.java)

### 4. Event update validation güçlendirilmeli

**Öncelik:** Orta

PATCH isteğinde alanların nullable olması doğrudur. Ancak boş metin değerleri, örneğin `name: ""`, mevcut validation yaklaşımıyla kabul edilebilir.

**Önerilen çözüm:**

- Metin alanlarında `@NotBlank` ve `@Size` kurallarını birlikte değerlendirin.
- PATCH için gönderilmeyen alan ile boş gönderilen alanın farkını koruyun.
- PUT ve PATCH davranışlarını dokümante edin.
- Geçersiz değerler service katmanına ulaşmadan reddedilsin.

İlgili dosya: [EventUpdateRequest.java](src/main/java/com/example/ticketsystem/dto/EventUpdateRequest.java)

### 5. Secret ve admin başlangıç ayarları güvenli varsayımlara sahip olmalı

**Öncelik:** Orta/Yüksek

`.env` dosyasının ignore edilmesi olumlu. Ancak JWT secret, veritabanı parolası veya admin parolası eksik ya da güvensiz olduğunda uygulamanın nasıl davranacağı daha kesin hale getirilmeli.

**Önerilen çözüm:**

- JWT secret boşsa uygulamanın başlamasını engelleyin.
- Admin bootstrap için boş veya varsayılan parola kabul etmeyin.
- Production ortamında geliştirme parolalarının kullanılmaması gerektiğini README’de açıkça belirtin.
- `.env.example` içinde yalnızca örnek değerler bulundurun.

İlgili dosyalar: [application.properties](src/main/resources/application.properties), [AdminBootstrap.java](src/main/java/com/example/ticketsystem/config/AdminBootstrap.java), [.env.example](.env.example)

### 6. Hata cevapları standartlaştırılmalı

**Öncelik:** Orta

Birçok iş kuralı genel `BusinessException` üzerinden dönüyor. Bu yapı başlangıç için yeterli olsa da istemcinin hatanın türünü anlamasını zorlaştırabilir.

**Önerilen çözüm:**

Hata cevaplarına sabit bir `code` alanı ekleyin:

```json
{
  "code": "EVENT_SOLD_OUT",
  "message": "Etkinlikte boş koltuk bulunmamaktadır."
}
```

Örnek kodlar:

- `EVENT_NOT_FOUND`
- `EVENT_SOLD_OUT`
- `TICKET_ALREADY_CANCELLED`
- `INVALID_REFRESH_TOKEN`
- `DUPLICATE_FAVORITE`

İlgili dosya: [GlobalExceptionHandler.java](src/main/java/com/example/ticketsystem/exception/GlobalExceptionHandler.java)

## Şimdilik Yapılması Gerekmeyenler

Aşağıdaki konular, trafik düşük olacağı için şu aşamada gerekli değildir:

- Redis tabanlı rate limiter
- Kafka veya mesaj kuyruğu
- Outbox pattern
- Dağıtık scheduled job lock
- Çoklu instance koordinasyonu
- Büyük ölçekli event-driven bildirim altyapısı
- İleri seviye cache sistemi

Mevcut `ConcurrentHashMap` tabanlı login rate limiter ve scheduled job yapısı tek uygulama instance’ı için yeterlidir.

## Daha Sonraki Geliştirmeler

Bunlar mevcut proje için acil değildir, ancak sistem büyürse değerlendirilebilir:

1. Satın alma işlemlerinde `Idempotency-Key` desteği.
2. Event, user ve ticket listelemelerinde pagination.
3. İptal edilmiş biletlerin silinmesi yerine arşivlenmesi.
4. Event ve ticket geçmişi için basit audit kayıtları.
5. OpenAPI üzerinde response, hata kodu ve örnek payload açıklamalarının artırılması.

## Uygulama Sırası

Önerilen çalışma sırası:

1. README ve Docker komutlarını tek akışta birleştirin.
2. Test scriptlerindeki sabit admin parolasını kaldırın.
3. Ticket koltuk benzersizliği kuralını güçlendirin.
4. Event update validation kurallarını düzeltin.
5. Secret ve admin başlangıç kontrollerini sıkılaştırın.
6. Hata response’larına standart `code` alanı ekleyin.
7. Sonraki aşamada idempotency ve pagination ekleyin.

## Sonuç

Projenin mevcut mimarisi düşük trafikli bir öğrenci projesi için yeterli ve başarılıdır. Şu an büyük altyapı değişikliklerine ihtiyaç yoktur. En doğru yaklaşım; mevcut yapıyı koruyarak veri tutarlılığı, güvenli yapılandırma, dokümantasyon ve API davranışlarındaki küçük ama etkili sorunları düzeltmektir.
