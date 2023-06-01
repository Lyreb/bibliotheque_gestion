# bibliotheque_gestion

arborescence
/projet_biblio
  |
  |---server-biblio
  |
  |---front-biblio

pour lancer le back --> dans le fichier back : "docker-compose up"
si processus déjà en cours :
  - lsof -i :3000
  - kill -9 [PID]

lancer le front
  - installer les nodes : npm install
  - lancer le front : npm run start
