# API REST de gestion des membres

Base d'URL : __/api/v1/rest/members__

## GET /api/v1/rest/members

- __Récupère la liste des membres.__
- Codes retour possibles :
  - __200__ : OK
  - __500__ : Erreur serveur

### Exemple de réponse

```
[
  {
    "id": "61dbc9a116781975a3d10dc7",
    "name": "Planck",
    "firstname": "Max",
    "birthday": "1968-09-04"
  },
  {
    "id": "61dbc9a116781975a3d10dc8",
    "name": "Solvay",
    "firstname": "Ernest",
    "birthday": "1999-12-10"
  }
]
```

## POST /api/v1/rest/members

- __Créer un nouveau membre.__
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
  "name": "Freeman",
  "firstname": "Martial",
  "birthday": "1999-12-10"
}
```

### Exemple de réponse

```
{
  "id": "61dbcb1116781975a3d10df1",
  "name": "Freeman",
  "firstname": "Martial",
  "birthday": "1999-12-10"
}
```

## GET /api/v1/rest/members/_:id_

- __Récupère les informations d'un membre, incluant ses prêts mentionnant la copie empruntée.__
- Paramètres d'URL :
  - _:id_ : l'identifiant de l'utilisateur
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de membre inconnu
  - __500__ : Erreur serveur

### Exemple de réponse

```
{
  "id": "61dbc9a116781975a3d10dc7",
  "name": "Planck",
  "firstname": "Max",
  "birthday": "1968-09-04",
  "loans": [
    {
      "id": "61dcb7b2c51efa5f19491fd6",
      "bookCopy": {
        "id": "61dcb7b2c51efa5f19491fc6",
        "state": "GOOD",
        "removed": false,
        "available": false
      },
      "loanDateTime": "2021-06-20T10:48:00",
      "initialState": "NEW",
      "returnDateTime": "2021-06-30T10:48:00",
      "returnState": "GOOD"
    },
    {
      "id": "61dcb7b2c51efa5f19491fd8",
      "bookCopy": {
        "id": "61dcb7b2c51efa5f19491fca",
        "state": "VERY_GOOD",
        "removed": false,
        "available": true
      },
      "loanDateTime": "2021-01-19T12:51:00",
      "initialState": "NEW",
      "returnDateTime": "2021-02-17T12:51:00",
      "returnState": "VERY_GOOD"
    }
  ]
}
```

## PUT /api/v1/rest/members/_:id_

- __Modifie les informations d'un membre.__ Ne modifie que les informations suivantes : name, firstname, birthday. Si name, firstname ou birthday n'est pas renseignée, est nulle ou vide, l'attribut n'est pas mis à jour. Retourne les informations du membre mises à jour détaillées (incluant ses prêts mentionnant la copie empruntée).
- Paramètres d'URL :
  - _:id_ : l'identifiant de l'utilisateur
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide
  - __404__ : Identifiant de membre inconnu
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "id": "61dbcb1116781975a3d10df1",
  "name": "Freeman",
  "firstname": "Morgan",
  "birthday": "1980-10-01"
}
```

### Exemple de réponse

__Idem que pour GET /members/_:id___

## DELETE /api/v1/rest/members/:id

- __Supprime un membre.__ Ne supprime pas ses prêts, ces derniers seront sans membre a posteriori.
- Paramètres d'URL :
  - _:id_ : l'identifiant de l'utilisateur
- Aucune donnée de réponse attendue
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de membre inconnu
  - __500__ : Erreur serveur
