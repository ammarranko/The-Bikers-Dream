# BikeSOEN343
SOEN 343 Project – The Biker's Dream

---

## Team Members
| GitHub Username | Full Name           | Student ID |
|-----------------|------------------|-----------|
| @masspaol       | Massimo Paolini   | 40280323  |
| @elif5446       | Elif Sag Sesen    | 40283343  |
| @afkcya         | Ya Yi (Yuna) Chen | 40286042  |
| @RyanCheung03   | Ryan Cheung       | 40282200  |
| @ammarranko     | Ammar Ranko       | 40281232  |
| @nicoledesigns  | Nicole Antoun     | 40284018  |

---

## Requirements
Before running the application, ensure you have the following installed:

- **JDK**: version 25  
- **Maven**: version 3.5.6  
- **Node.js & npm** (for React frontend)  

---

## Running the Application

1. **Open two terminal windows**:
   - One for the frontend  
   - One for the backend  

2. **Backend Setup** (in the backend terminal):
   ```bash
   cd backend/tbd
   mvn clean install    # Run this only if new dependencies were added
   mvn spring-boot:run
3. **Frontend Setup** (in the frontend terminal):
   ```bash
   cd frontend
   npm install          # Run this only if new dependencies were added
   npm start
4. Once both backend and frontend are running, the application will be available in your browser at the default React port <code>http://localhost:3000</code>.
