# VirusTotal Tekshiruvchi (vtChecker)

* Muallif: Komil Hamzayev <hamzayevkomil52@gmail.com>
* Manba kodi (Source code): [GitHub Ombori](https://github.com/komilblindev/vtChecker)
* NVDA mosligi: 2024.1.0 va undan yuqori

Bu plagin fayllar, URL manzillar yoki Xeshlarni VirusTotal API orqali osongina tekshirish imkonini beradi.

## Sozlash
1. [VirusTotal](https://www.virustotal.com/) saytidan bepul ro'yxatdan o'ting.
2. Profilingiz -> API Key bo'limiga kirib, kalitingizni nusxalang.
3. NVDA Sozlamalari -> VirusTotal bo'limini oching, API kalitni joylashtiring va parametrlarni sozlang (avto-yuklash, tovushli signallar va boshqalar).

## Foydalanish va tezkor klavishlar
Standart holatda hech qanday klavish belgilanmagan. Ularni o'zingiz belgilashingiz kerak:
1. **NVDA Menyusi -> Parametrlar -> Kiritish imo-ishoralari (Input Gestures)** bo'limini oching.
2. **VirusTotal** toifasini topib kengaytiring.
3. Quyidagi buyruqlarga o'zingizga qulay klavishlarni biriktiring:
   * **Fokusdagi faylni tekshirish:** Windows Explorer-da tanlangan faylni tekshiradi.
   * **Qo'lda tekshirish oynasi:** URL, IP yoki SHA-256 xeshini kiritish uchun maxsus oyna ochadi.

### Faylni tekshirish
Fayl ustida belgilangan klavishni bosganingizda:
* **1 marta bosish:** NVDA qisqacha xulosani aytadi (masalan, 0/91) va VT havolasi bilan birga to'liq batafsil hisobot avtomatik xotiraga (buferga) nusxalanadi.
* **2 marta tez bosish:** Barcha antivirus tizimlarining batafsil hisobotini o'qish imkonini beruvchi to'liq oyna ochiladi.

Agar fayl VirusTotal bazasida umuman yo'q bo'lsa, plagin uni avtomatik ravishda yuklashi mumkin (agar sozlamalardan yoqilgan bo'lsa), orqa fonda navbatni kutadi va natijalar tayyor bo'lishi bilanoq sizga ovozli va matnli xabar beradi.


### API Limits
Eslatma: VirusTotal bepul API standarti 1 daqiqada 4 tagacha va kuniga 500 tagacha so'rov yuborish imkonini beradi. Limitlaringiz holatini plagin sozlamalaridan tekshirishingiz mumkin.
