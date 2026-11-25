# Registracija i prijava

Za izdavanje JWT tokena podesi `PARKING_JWT_SECRET` u okruženju procesa koji pokreće backend. U IntelliJ-u se ta promenljiva dodaje u **Run/Debug Configurations → Environment variables**. Isti ključ možeš zadati kroz Spring opciju `parking.auth.jwt-secret` u spoljašnjoj konfiguraciji.

Ključ mora biti standardni Base64 zapis najmanje 32 nasumična bajta. Primer generisanja ključa u PowerShell-u:

```powershell
$key = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($key)
$rng.Dispose()
[Convert]::ToBase64String($key)
```

Dobijenu vrednost čuvaj izvan repozitorijuma. Backend izdaje tokene potpisane algoritmom HS256 koji važe 3600 sekundi. Ako ključ nije podešen, prijava sa ispravnim kredencijalima vraća HTTP 503. Ako je podešen nevažeći ključ, backend prijavljuje grešku pri pokretanju.

## PSP potvrda plaćanja

PSP poziva `POST /api/parking-orders/payment-callback` preko HTTPS-a. Šalje `Content-Type: application/json` i `X-PSP-Callback-Key` sa zajedničkom tajnom. U okruženju parking backenda podesi `PARKING_PSP_CALLBACK_SECRET` ili Spring opciju `parking.psp.callback-secret` u spoljašnjoj konfiguraciji. Za tajnu možeš koristiti zasebnu nasumičnu Base64 vrednost generisanu prethodnom PowerShell komandom.

```json
{"orderId":12,"pspTransactionId":"psp-tx-784","status":"SUCCESS","paidAmount":240.00,"currency":"RSD"}
```

Sva polja su obavezna. `status` je `SUCCESS` ili `FAILED`. `pspTransactionId` je identifikator od najviše 255 znakova koji PSP ponavlja pri ponovnoj dostavi. `paidAmount` je JSON broj u osnovnoj jedinici valute, a `currency` mora biti isti troslovni kod kao na nalogu. Za `FAILED` pošalji iznos i valutu pokušaja plaćanja sa naloga, iako sredstva nisu naplaćena.

Uspešna obrada vraća HTTP 200 sa `orderId`, statusom `PAID` i objektom `session`. Objekat sadrži `id`, `userId`, `zoneId`, `vehicleId`, `startsAt`, `expiresAt`, `paidAmount`, `currency` i `pspTransactionId`. Vremena su u ISO 8601 UTC formatu. Obrađen neuspeh vraća HTTP 200 sa statusom `PAYMENT_FAILED` i `session: null`.

Ponovna dostava istog ishoda za isti nalog i ID transakcije vraća isti rezultat. Prvi prihvaćeni ishod zaključuje nalog. HTTP 400 označava neispravan zahtev, nepodudaranje iznosa ili valute ili potvrdu koja je u konfliktu sa već obrađenim ishodom. HTTP 401 označava nedostajući ili nevažeći ključ, a HTTP 404 nepostojeći nalog.
