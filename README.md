# Email System

## 1. Definirea Sistemului

Sistemul reprezinta o platforma de email cu interfata **CLI**. Aplicatia functioneaza pe o arhitectura client-server stabila, conexiunea dintre client si server se realizeaza prin **sockets**, iar serverul este aceasi aplicatie, aflata in "server mode". Pentru a gestiona mai multi utilizatori simultan, serverul utilizeaza **multithreading**, asigurand o performanta optima si o raspundere rapida. De asemenea, platforma integreaza o baza de date cu **JDBC** care asigura persistenta datelor si pastrarea mesajelor in timp.

### Actiuni/Interogari Disponibile:

1. Autentificare utilizator (Login)
2. Inregistrare utilizator nou (Register)
3. Trimitere email catre un destinatar
4. Vizualizare inbox
5. Vizualizare folder spam
6. Marcare email ca citit/necitit
7. Stergere email
8. Mutare email intre foldere
9. Adaugare contact in lista de contacte
10. Trimitere email catre contact din lista
11. Deconectare de la sistem
12. Vizualizare status emailuri (citit/necitit)

### Tipuri de Obiecte:

1. `User` - Gestioneaza informatiile utilizatorului
2. `Email` - Reprezinta structura unui email
3. `Folder` - Container pentru organizarea emailurilor
4. `Contact` - Gestioneaza informatiile contactelor
5. `EmailClient` - Gestioneaza conexiunea client
6. `EmailServer` - Coordoneaza comunicarea intre clienti
7. `DateTime` - Gestioneaza informatiile temporale
8. `Auth` - Gestioneaza autentificarea utilizatorilor

## 2. Implementare

### Arhitectura Sistemului

Sistemul este implementat folosind o arhitectura client-server in Java, cu urmatoarele componente principale:

#### Server Component
```java
public class EmailServer {
    private ServerSocket serverSocket;
    private final int PORT = 12345;
    private Map<String, Map<String, Folder>> userFolders;
    private Map<String, ObjectOutputStream> clientOutputStreams;
}
```

#### Client Component
```java
public class EmailClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Map<String, Folder> folders;
}
```

### Flow-ul Aplicatiei

1. **Initializare**:
   - Serverul porneste si asculta pe portul 12345
   - Clientii se pot conecta la server

2. **Autentificare**:
   - Utilizatorul poate alege intre login si register
   - Credentialele sunt validate de server
   - Dupa autentificare, se stabileste o conexiune persistenta

3. **Operatiuni Email**:
   - Trimitere email:
     ```java
     public boolean sendEmail(String to, String subject, String content) {
         Email email = new Email(userEmail, to, subject, content);
         out.writeObject("SEND_EMAIL:" + userEmail);
         out.writeObject(email);
     }
     ```
   - Primire email:
     - Serverul distribuie emailurile catre destinatari
     - Clientii primesc notificari in timp real

4. **Gestionare Foldere**:
   - Fiecare utilizator are foldere predefinite (inbox, spam)
   - Emailurile pot fi mutate intre foldere
   - Sistemul mentine starea emailurilor (citit/necitit)

### Caracteristici Tehnice

1. **Comunicare Asincrona**:
   - Utilizare de ThreadPool pentru gestionarea conexiunilor multiple
   - Implementare de cozi pentru procesarea mesajelor

2. **Persistenta Datelor**:
   - Obiectele sunt serializabile pentru stocare
   - Gestionare eficienta a sesiunilor utilizator
   - Utilizare JDBC pentru persistenta datelor in baza de date

3. **Securitate**:
   - Validare email (@mihail.ro sau @example.com)
   - Autentificare utilizator obligatorie
   - Separare date intre utilizatori

4. **Logging**:
   - Implementare de logging pentru monitorizarea activitatii sistemului
   - Inregistrarea evenimentelor importante pentru audit si depanare

### Utilizare

Pentru a porni aplicatia:

1. Pornire server:
```bash
java Main
# Selectati optiunea 1
```

2. Pornire client:
```bash
java Main
# Selectati optiunea 2
```

### Dependinte

- Java Runtime Environment (JRE) 8 sau superior
- Biblioteca standard Java pentru networking si I/O
- JDBC pentru conectarea la baza de date

### Note de Implementare

- Sistemul foloseste Java Sockets pentru comunicare retea
- Implementare thread-safe pentru operatiuni concurente
- Gestionare eficienta a resurselor si conexiunilor
- Interfata utilizator bazata pe consola pentru simplitate si robustete 