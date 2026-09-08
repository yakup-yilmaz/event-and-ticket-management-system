# Düzeltilmesi Gerekenler Raporu

## Kapsam

Bu rapor, proje son haliyle tekrar incelendikten ve Docker Desktop açıldıktan sonra gerçek Docker build denemesi yapıldıktan sonra hazırlanmıştır.

Proje düşük trafikli bir öğrenci projesi olarak değerlendirilmektedir. Production deployment, Redis, Kafka, outbox pattern, dağıtık lock ve benzeri yüksek ölçek altyapıları bu raporun kapsamı dışındadır.

## Genel Sonuç

Uygulamanın Java tarafı derleniyor ve jar olarak başarıyla çalışıyor. PostgreSQL bağlantısı kurulabiliyor ve gerçek endpoint testlerinde temel akışlar çalışıyor.

Ancak Docker build şu anda başarısız oluyor. Ayrıca ticket iptalinde nadir ama gerçek bir eşzamanlılık problemi bulunuyor.

## 1. Docker Build Başarısız Oluyor

**Öncelik: Yüksek**

Gerçek çalıştırılan komut:

```powershell
docker compose build
```

Build şu aşamada başarısız oldu:

```text
RUN mvn -B clean package
```

Hata özeti:

```text
Unable to determine Dialect without JDBC metadata
```

Dockerfile build aşamasında testleri de çalıştırıyor. Ancak image oluşturulurken:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

değişkenleri build container'ına aktarılmıyor. `TicketsystemApplicationTests` Spring context başlattığı için veritabanı ayarlarını bekliyor ve test başarısız oluyor.

### Önerilen çözüm

Düşük trafikli öğrenci projesi için en basit çözüm Dockerfile içindeki build komutunu şu şekilde değiştirmektir:

```dockerfile
RUN mvn -B clean package -DskipTests
```

Testler local ortamda ayrıca çalıştırılabilir:

```powershell
./mvnw.cmd clean test
```

Uygulama çalışma zamanında gerekli environment değişkenlerini zaten `docker-compose.yml` üzerinden alacaktır.

Bu değişiklik Docker image build sorununu çözer ve mevcut mimariye yeni teknoloji eklemez.

## 2. Ticket İptalinde Eşzamanlılık Problemi

**Öncelik: Orta**

[TicketServiceImpl.java](src/main/java/com/example/ticketsystem/service/TicketServiceImpl.java) içinde ticket önce kilitsiz okunuyor, daha sonra event pessimistic lock ile okunuyor.

Aynı ticket için iki iptal isteği aynı anda gelirse teorik olarak iki işlem de ticket durumunu `PURCHASED` görebilir. İlk işlem kapasiteyi artırdıktan sonra ikinci işlem ticket durumunu yeniden kontrol etmeden kapasiteyi tekrar artırabilir.

Olası sonuç:

```text
availableSeats > totalSeats
```

### Önerilen çözüm seçenekleri

En basit çözüm, event lock alındıktan sonra ticket'ı tekrar okumaktır:

1. Ticket'ın event ID'sini alın.
2. Event'i pessimistic lock ile okuyun.
3. Ticket'ı transaction içinde tekrar okuyun.
4. Ticket status'i `CANCELLED` ise işlemi sonlandırın.
5. Status `PURCHASED` ise iptal edip kapasiteyi bir kez artırın.

Alternatif olarak ticket repository'de pessimistic lock kullanan bir sorgu eklenebilir.

Bu problem düşük trafik nedeniyle sık karşılaşılmayabilir; fakat düzeltilmesi iş kuralı doğruluğu açısından faydalıdır.

## 3. Veritabanı Constraint ve Index Uyarıları

**Durum: Büyük ölçüde açıklanmış, acil kod hatası değil**

Uygulama açılırken Hibernate şu tip uyarılar üretmişti:

```text
constraint "uk_ticket_event_seat" ... does not exist, skipping
constraint "uk_event_name_date" ... does not exist, skipping
constraint "uk_favorite_user_event" ... does not exist, skipping
```

Database'i drop ettiğinizi belirttiniz. Bu durumda eski veritabanı nesneleri zaten bulunmadığı için Hibernate'in eski constraint veya index'leri kaldırmaya çalışırken `does not exist, skipping` yazması beklenebilir.

Entity tarafındaki constraint tanımları mevcut:

- Ticket event-seat unique constraint
- Event name-date unique constraint
- Favorite user-event unique constraint
- User email unique constraint
- Token ve ticket index'leri

### Önerilen kontrol

Uygulama yeniden başladıktan sonra PostgreSQL içinde constraint'lerin oluştuğunu kontrol edin. Eğer database tamamen temiz oluşturulduysa ve uygulama `ddl-auto=update` ile açıldıysa, büyük olasılıkla entity tanımlarına göre yeniden oluşturulmuşlardır.

Bu konu migration eksikliği olarak değerlendirilmemiştir. Yalnızca mevcut şemanın güncel olduğunu kontrol etmek yeterlidir.

## 4. Varsayılan Admin Parolası

**Öncelik: Düşük**

Uygulama varsayılan geliştirme parolasıyla başlarken uyarı veriyor:

```text
Varsayılan geliştirme parolası (ChangeMe123!) kullanılmaktadır.
```

Production'a çıkılmayacağı için bu öğrenci projesinde kritik değildir. Yine de `.env.example` dosyasının yalnızca local geliştirme amacıyla olduğu README'de açıkça belirtilmelidir.

Şu anki davranış kabul edilebilir:

- Boş parola reddediliyor.
- Varsayılan parola için uyarı loglanıyor.
- Gerçek parola `.env` içinden verilebiliyor.

Bu maddeyi şimdilik düşük öncelikte bırakabilirsiniz.

## 5. HELP.md Sürüm Bilgisi

**Öncelik: Düşük**

[HELP.md](HELP.md) içinde Spring Boot `4.1.1` dokümantasyon bağlantıları bulunuyor. Proje ise Spring Boot `3.3.2` kullanıyor.

Bu çalışma zamanı hatası değildir. İsterseniz bağlantıları Spring Boot 3.3.x dokümantasyonuna güncelleyebilirsiniz.

## Gerçekleştirilen Testler

### Başarılı kontroller

- `mvnw.cmd clean package -DskipTests`
- Jar üzerinden uygulama başlatma
- PostgreSQL bağlantısı
- Authentication endpointleri
- User endpointleri
- Event endpointleri
- Favorite endpointleri
- Ticket endpointleri
- Notification endpointleri
- Swagger UI
- OpenAPI JSON
- Yetkisiz erişim kontrolleri
- Boş PATCH validation kontrolü
- Tokensız logout kontrolü

### Beklenen sonuç veren negatif senaryolar

- Yetkisiz user listesi: `403`
- Tokensız logout: `401`
- Boş PATCH isteği: `400`
- Public event GET: `200`

### Başarısız kontrol

```powershell
docker compose build
```

Sebep: Docker image build sırasında testlerin gerekli datasource ve JWT environment değişkenlerini alamaması.

## Şu Anda Yapılması Gerekenler

Önerilen sıra:

1. [Dockerfile](Dockerfile) içindeki `mvn -B clean package` komutunu `mvn -B clean package -DskipTests` yapın.
2. Docker Desktop açıkken tekrar `docker compose build` çalıştırın.
3. Ardından `docker compose up` ile uygulamayı başlatın.
4. Ticket cancellation akışında event lock alındıktan sonra ticket status'ünü tekrar kontrol edin.
5. PostgreSQL'de entity constraint'lerinin oluştuğunu kontrol edin.
6. İsterseniz HELP.md Spring Boot 3.3.2 bağlantılarını kullanacak şekilde güncelleyin.

## Şimdilik Yapılması Gerekmeyenler

Bu proje için şu aşamada gerekli değildir:

- Redis
- Kafka
- Outbox pattern
- Distributed lock
- Çoklu uygulama instance koordinasyonu
- Cache sistemi
- İleri seviye pagination
- Production secret yönetimi

## Sonuç

Projede kritik sayılabilecek kalan konu Docker build'in başarısız olmasıdır. Bunun nedeni kodun paketlenememesi değil, Docker build sırasında test context'inin veritabanı ayarlarını bulamamasıdır.

Database'i drop etmeniz, önceki constraint/index `does not exist, skipping` uyarılarını açıklıyor. Bu uyarılar tek başına uygulamanın bozuk olduğu anlamına gelmez.

Dockerfile düzeltmesi ve ticket cancellation concurrency kontrolü tamamlandığında, düşük trafikli öğrenci projesi için önemli bir sorun kalmayacaktır.
