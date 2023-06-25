# Driver safety - Gateway
Codice per applicazione Android che svolge il gateway nell'architettura Iot per la safety del driver.
Gli altri componenti sono:
- Simulatore: simulatore dati OBD che li fornisce tramite BLE e websocket. Il codice si trova a questo [link]()
- Watch: fornisce il battito cardiaco tramite BLE. Il codice si trova a questo [link]()
- Backend: servizio che prende i dati e li rende disponibili tramite REST API. Il codice si trova a questo [link]()
- Dashboard: servizio per visualizzare i dati. Il codice si trova a questo [link]()

# Esecuzione

Per eseguire il programma:
- Svolgere i preparativi
- Avviare il simulatore
- Avviare il watch
- Avviare il backend
- Eseguire il programma

## Preparativi
Prima di eseguire l'applicazione, bisogna scrivere i valori corretti nel file di configurazione (a partire dalla root del progetto) `./app/src/main/java/a/b/Config.kt`. Bisogna specificare:
1. Informazioni sul web socket del simulatore: indirizzo ip del device dove si sta eseguendo il simulatore e porta per il web socket; la porta deve corrispondere alla porta usata all'interno del codice del simulatore (analizzare il README del relativo progetto).
2. Informazioni sul bluetooth del simulatore: indirizzo bluetooth del device dove si sta eseguendo il simulatore.
3. Assicurarsi che gli UUID del servizio e delle caratteristiche corrispondano a quelli specificati all'interno del file di configurazione del progetto del simulatore.
4. Informazioni sul bluetooth low energy del watch: indirizzo bluetooth del device che svolge la funzionalità di watch
5. Assicurarsi che gli UUID del servizio e della caratteristica del battito corrispondono a quelli specificati nel progetto del watch

## Avviare il simulatore
Avviare il simulatore come specificato nel README del relativo progetto.

## Avviare il watch
Avviare il simulatore come specificato nel README del relativo progetto.

## Avviare il backend 
Avviare il simulatore come specificato nel README del relativo progetto.

## Esecuzione usando Android Studio
- Collegare il proprio telefono Android e metterlo in modalità debugging USB.
- Eseguire il codice

All'inizio l'applicazione si avvierà mostrando dei valori vuoti per i vari parametri analizzati:
![Applicazione all'avvio]()

Dopo aver cliccato sul bottone "Start", avverrà la connessione e i valori letti verranno mostrati su schermo:
![Applicazione durante l'esecuzione]()

# Spiegazione codice

