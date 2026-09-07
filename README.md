
# 🎟️ Ticket System API (Bilet Satış & Yönetim Sistemi)

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.2-6DB33F?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-Security-black?style=for-the-badge&logo=jsonwebtokens)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)
![Swagger](https://img.shields.io/badge/Swagger-OAS3-85EA2D?style=for-the-badge&logo=swagger)

Bu proje, eşzamanlı bilet satış senaryolarını güvenli şekilde yönetmek için tasarlanmış, bildirimleri uygulama içindeki servis entegrasyonu üzerinden oluşturan kapsamlı bir **Backend API** sistemidir.

Standart CRUD işlemlerinin ötesine geçerek; **Veri tutarlılığı**, **Dinamik fiyatlandırma algoritmaları** ve **Gelişmiş JWT Güvenliği** gibi modern yazılım mühendisliği pratiklerini barındırır.

---

## 🚀 Öne Çıkan Mimari Özellikler (Engineering Highlights)

- **Eşzamanlılık Kontrolü (Pessimistic Lock):** Aynı anda bilet almak isteyen kullanıcılar arasında "Race Condition" oluşmaması için veritabanı seviyesinde `Pessimistic Lock` kullanılmış, aynı koltuğun iki kişiye satılması engellenmiştir.
- **Tasarım Kalıpları (Strategy Pattern):** Etkinliklerdeki doluluk oranına göre fiyatı dinamik olarak artıran algoritma (`OccupancyBasedPricingStrategy`), if-else bloklarına boğulmadan **Strategy Design Pattern** ile izole edilmiştir.
- **Entegre Bildirim Sistemi (Integrated Notifications):** Sistemdeki "Favoriler" modülü sadece bir listeleme aracı değildir. Etkinlik fiyatı veya tarihi değiştiğinde, etkinlik iptal edildiğinde ya da durum değiştiğinde favoriye alan kullanıcılara ve gerektiğinde bilet sahiplerine, ilgili servislerin doğrudan çağrılmasıyla veritabanına bildirim kaydı oluşturulur.
- **Gelişmiş JWT Güvenliği (Token Blacklist):** Çıkış yapan veya şifresi sıfırlanan kullanıcıların token'ları **Kara Listeye** alınır. İptal edilmiş tokenlarla erişim kalıcı olarak kesilir.
- **Performans Optimizasyonu (N+1 Problemi):** Binlerce bileti/bildirimi olan kullanıcılara ait listelemelerde ORM darboğazlarını önlemek için repository katmanında `JOIN FETCH` optimizasyonları yapılmıştır.

---

## 🛠️ Kullanılan Teknolojiler

- **Backend:** Java 17, Spring Boot 3.3.2
- **Veritabanı:** PostgreSQL 16 (Hibernate 6.5 & Spring Data JPA)
- **Güvenlik:** Spring Security, JSON Web Token (JWT), BCrypt
- **Araçlar:** MapStruct (DTO Mapping), Lombok, Maven, Docker & Docker Compose
- **Dokümantasyon:** OpenAPI (Swagger 3)

---

## 📦 Kurulum ve Çalıştırma (1-Click Setup)

Docker entegrasyonu sayesinde ekstra bir veritabanı kurmanıza gerek yoktur.

### 1. Klonlama ve Ayarlar
```bash
git clone https://github.com/KULLANICI_ADINIZ/ticketsystem.git
cd ticketsystem
cp .env.example .env
```
### 2. Başlatma
Aşağıdaki komutları sırasıyla çalıştırın:
```bash
docker-compose up -d
./mvnw spring-boot:run
```

Proje başarıyla başladığında **Swagger API Dokümantasyonuna** şu adresten ulaşabilirsiniz:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---

## 🔌 API Uç Noktaları (Endpoints)

Tüm API uç noktaları Swagger arayüzü üzerinden interaktif olarak test edilebilir.

### 🔐 Kimlik Doğrulama (Auth)
| Metot | Uç Nokta | Açıklama |
| :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Sisteme yeni kullanıcı kaydeder. |
| `POST` | `/api/v1/auth/login` | Giriş yapar. Access ve Refresh Token döner. |
| `POST` | `/api/v1/auth/refresh` | Süresi dolan Access token'ı yeniler. |
| `POST` | `/api/v1/auth/logout` | Token'ı kara listeye alarak güvenli çıkış yapar. |

### 🎭 Etkinlikler (Events)
| Metot | Uç Nokta | Açıklama |
| :--- | :--- | :--- |
| `GET` | `/api/v1/events/active` | Satışa açık olan etkinlikleri listeler. |
| `POST` | `/api/v1/events` | **(ADMIN)** Yeni bir etkinlik ekler (Dinamik/Sabit fiyat). |
| `PUT` | `/api/v1/events/{id}` | **(ADMIN)** Etkinlik detaylarını günceller. |

### 🎫 Biletler (Tickets)
| Metot | Uç Nokta | Açıklama |
| :--- | :--- | :--- |
| `POST` | `/api/v1/tickets/buy` | Etkinliğe bilet alır (Otomatik Sıralı Koltuk Ataması). |
| `POST` | `/api/v1/tickets/{id}/cancel` | Bileti iptal eder ve koltuğu sisteme iade eder. |
| `GET` | `/api/v1/tickets/my-tickets` | Kullanıcının satın aldığı tüm biletleri listeler. |

### ❤️ Favoriler (Favorites)
| Metot | Uç Nokta | Açıklama |
| :--- | :--- | :--- |
| `POST` | `/api/v1/favorites/{eventId}` | Etkinliği favorilere ekler (Tükendiğinde bildirim almak için). |
| `DELETE`| `/api/v1/favorites/{eventId}` | Etkinliği favorilerden çıkarır. |
| `GET` | `/api/v1/favorites` | Kullanıcının favori listesini getirir. |

### 🔔 Bildirimler (Notifications)
| Metot | Uç Nokta | Açıklama |
| :--- | :--- | :--- |
| `GET` | `/api/v1/notifications/unread` | Okunmamış sistem bildirimlerini listeler. |
| `POST` | `/api/v1/notifications/{id}/read` | Bildirimi "okundu" olarak işaretler. |
| `POST` | `/api/v1/notifications/read-all`| Tüm bildirimleri tek tıkla okundu yapar. |

---
