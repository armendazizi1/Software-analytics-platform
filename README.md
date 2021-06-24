A software analytics platform that does the following: 

* Visualize and easily navigate the change history of a system
* Mark bug- inducing commits with the SZZ algorithm
* Mine information about Pull Requests
* Profile software developers: contribution, PR, bugs introduced, expertise 
* Just-in-time defect prediction

# Backend Setup

Before running the application you need to make sure that the database is working properly and that it is active.
MongoDB has been used as a database.


## Usage

### Using Intellij IDE

Simply run the application with the appropriate tools provided by Intellij IDE.

### Using Terminal

Just go inside the backend folder and execute the following commands:

```bash
mvn compiler:compile
mvn spring-boot:run
```

After the application has started correctly, you can contact it at the following address [localhost:8080/](https://localhost:8080/).


# Frontend Setup

```bash
cd frontend/
npm install
npm start or yarn start
run `localhost:3000` on your browser (chrome preferable).
```


# Running ck library

cd backend/libs
java -jar ck-0.6.3-SNAPSHOT-jar-with-dependencies.jar <repo-dir> true 0 false
  
  
## Authors

* **Gabriele Zorloni**  
* **Armend Azizi**    
* **Susanna Ardigo**    
* **Nimisha Manjali**   
