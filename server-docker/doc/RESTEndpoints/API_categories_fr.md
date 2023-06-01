# API REST de gestion des catégories de livres.

Base d'URL : __/api/v1/rest/categories__

## GET /api/v1/rest/categories

- Récupère les catégories
- __Récupère la liste des catégories.__
- Codes retour possibles :
  - __200__ : OK
  - __500__ : Erreur serveur

### Exemple de réponse

```
[
  {
    "id": "61dbc9a116781975a3d10dcc",
    "code": "polar",
    "name": "Policier",
    "adultOnly": false
  },
  {
    "id": "61dbc9a116781975a3d10dcd",
    "code": "love",
    "name": "Amour",
    "adultOnly": false
  }
]
```

## POST /api/v1/rest/categories

- __Créer une nouvelle catégorie.__
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "code": "doc",
  "name": "Documentaire",
  "adultOnly": false
}
```

### Exemple de réponse

```
{
  "id": "61dbcfd116781975a3d10df2",
  "code": "doc",
  "name": "Documentaire",
  "adultOnly": false
}
```

## GET /api/v1/rest/categories/:id

- __Récupère les informations d'une catégorie.__
- Paramètres d'URL :
  - _:id_ : l'identifiant de la catégorie
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de catégorie inconnu
  - __500__ : Erreur serveur

### Exemple de réponse

```
{
  "id": "61dbcfd116781975a3d10df2",
  "code": "doc",
  "name": "Documentaire",
  "adultOnly": false
}
```

## PUT /api/v1/rest/categories/:id

- __Modifie les informations d'une catégorie__. Modifie les informations suivantes : code, name, adultOnly.
- Paramètres d'URL :
  - _:id_ : l'identifiant de la catégorie
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide
  - __404__ : Identifiant de catégorie inconnu
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "id": "61dbcfd116781975a3d10df2",
  "code": "docG",
  "name": "Documentaire généraliste",
  "adultOnly": true
}
```

### Exemple de réponse

```
{
  "id": "61dbcfd116781975a3d10df2",
  "code": "docG",
  "name": "Documentaire généraliste",
  "adultOnly": true
}
```

## DELETE /api/v1/rest/categories/:id

- __Supprime une catégorie.__ La catégorie est retirée de tous les livres qui lui sont associés, puis est supprimée.
- Paramètres d'URL :
  - _:id_ : l'identifiant de la catégorie
- Aucune donnée de réponse attendue
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de membre inconnu
  - __500__ : Erreur serveur
