# API REST de gestion des livres et de leurs copies

Base d'URL : __/api/v1/rest/books__

## GET /api/v1/rest/books

- __Récupère la liste des livres.__
- Paramètres de requête :
  - _isbn_ :
    - Optionnel, simple
    - Récupère le livres correspondant à l'isbn (si existant)
    - exemple: `?isbn=12345`
  - _title_ :
    - Optionnel, simple
    - Récupère les livres dont le titre est proche de celui fourni
    - exemple: `?title=le%20%petit%20prince`
  - _nbPages_ :
    - Optionnel, simple
    - Récupère les livres dont le nombre de pages est égal à nbPages ± 50
    - exemple: `?nbPages=100`
  - _cat_ :
    - Optionnel, multiple
    - Récupère les livres appartenant à au moins une des catégories dont le code a été fourni
    - exemple : `?cat=codeCat1&cat=codeCat2`
  - _child_ :
    - Optionnel, simple
    - Récupère les livres pouvant être emprunté par des enfant. La valeur associé au paramètre n'a pas d'importance.
    - exemple : `?child=true`
  - _available_ :
    - Optionnel, simple
    - Récupère les livres pouvant être emprunté (au moins une copie n'est pas en cours d'emprunt ni retirée)
    - exemple : `?available=true`
  - Tous ces paramètres peuvent être bien sûr combiné
    - exemple : `?title=petit%prince&cat=conte&child=true&available=true` : tous les ouvrages dont le titre est proche de "petit prince", appartenant à la catégorie de code "conte", pouvant être emprunté par des mineurs et disponibles à l'emprunt.
- Codes retour possibles :
  - __200__ : OK
  - __500__ : Erreur serveur

### Exemple de réponse

```
[
  {
    "id": "61dbc9a116781975a3d10dd1",
    "isbn": "9782070495023",
    "title": "Total Kheops",
    "editor": "Folio",
    "numOfPages": 284,
    "publicationYear": 2001,
    "authors": [
      {
        "firstname": "Jean-Claude",
        "lastname": "Izzo"
      }
    ],
    "categories": [
      {
        "id": "61dbc9a116781975a3d10dcc",
        "code": "polar",
        "name": "Policier",
        "adultOnly": false
      }
    ]
  },
  {
    "id": "61dbc9a116781975a3d10dd2",
    "isbn": "9782757800232 ",
    "title": "La cité des jarres",
    "editor": "Points",
    "numOfPages": 336,
    "publicationYear": 2006,
    "authors": [
      {
        "firstname": "Indriðason",
        "lastname": "Arnaldur"
      }
    ],
    "categories": [
      {
        "id": "61dbc9a116781975a3d10dcc",
        "code": "polar",
        "name": "Policier",
        "adultOnly": false
      },
      {
        "id": "61dbc9a116781975a3d10dcf",
        "code": "violence",
        "name": "Violent",
        "adultOnly": true
      }
    ]
  }
]
```

## POST /api/v1/rest/books

- __Créer un nouveau livre.__
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide
  - __409__ : Isbn déjà présent
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "isbn": "978-0262035613",
  "title": "Deep Learning",
  "editor": "The MIT Press",
  "numOfPages": 800,
  "publicationYear": 2016,
  "authors": [
    {
      "firstname": "Ian",
      "lastname": "Goodfellow"
    },
    {
      "firstname": "Yoshua",
      "lastname": "Bengio"
    },
    {
      "firstname": "Aaron",
      "lastname": "Courville"
    }
  ],
  "categories": [
    {
      "id": "61dbf30f16781975a3d10df5"
    }
  ]
},
```
_Commentaire_ : pour les categories, il est possible de ne mentionner que l'id de celle-ci. Si une catégorie inexistante est mentionnée (id null ou inconnu), celle-ci ne sera pas sauvegardée.

### Exemple de réponse

```
{
  "id": "61dbf47516781975a3d10dfd",
  "isbn": "978-0262035613b",
  "title": "Deep Learning",
  "editor": "The MIT Press",
  "numOfPages": 800,
  "publicationYear": 2016,
  "authors": [
    {
      "firstname": "Ian",
      "lastname": "Goodfellow"
    },
    {
      "firstname": "Yoshua",
      "lastname": "Bengio"
    },
    {
      "firstname": "Aaron",
      "lastname": "Courville"
    }
  ],
  "categories": [
    {
      "id": "61dbf30f16781975a3d10df5",
      "code": "sciences",
      "name": "Sciences",
      "adultOnly": false
    }
  ]
}
```

## GET /api/v1/rest/books/_:id_

- __Récupère les informations d'un livre, incluant ses copies.__
- Paramètres d'URL :
  - _:id_ : l'identifiant du livre
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de livre inconnu
  - __500__ : Erreur serveur

### Exemple de réponse

```
{
  "id": "61dbc9a116781975a3d10dd1",
  "isbn": "9782070495023",
  "title": "Total Kheops",
  "editor": "Folio",
  "numOfPages": 284,
  "publicationYear": 2001,
  "authors": [
    {
      "firstname": "Jean-Claude",
      "lastname": "Izzo"
    }
  ],
  "categories": [
    {
      "id": "61dbc9a116781975a3d10dcc",
      "code": "polar",
      "name": "Policier",
      "adultOnly": false
    }
  ],
  "copies": [
    {
      "id": "61dbc9a116781975a3d10dd5",
      "state": "GOOD",
      "removed": false,
      "available": true
    },
    {
      "id": "61dbc9a116781975a3d10dd6",
      "state": "GOOD",
      "removed": false,
      "available": true
    },
    {
      "id": "61dbc9a116781975a3d10dd7",
      "state": "USED",
      "removed": false,
      "available": true
    }
  ]
}
```

## PUT /api/v1/rest/books/:id

- __Modifie les informations d'un livre.__ Ne modifie que les informations suivantes : isbn, title, editor, numOfPages, publicationYear, authors et categories. Si isbn ou title n'est pas renseignée, est nulle ou vide, l'attribut n'est pas mis à jour. Retourne les informations du livre mises à jour détaillées (incluant ses copies).
- Paramètres d'URL :
  - _:id_ : l'identifiant du livre
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide
  - __404__ : Identifiant de livre inconnu
  - __409__ : Isbn déjà existant dans un autre livre
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "id": "61dbc9a116781975a3d10dd1",
  "isbn": "9782070495023b",
  "title": "Total Kheops 3",
  "editor": "Folio",
  "numOfPages": 384,
  "publicationYear": 2012,
  "authors": [
    {
      "firstname": "Pierre-Henri",
      "lastname": "Dedieur"
    }
  ],
  "categories": [
    {
      "id": "61dbc9a116781975a3d10dcc",
      "code": "polar",
      "name": "Policier",
      "adultOnly": false
    },
    {
      "id": "61dbc9a116781975a3d10dcf",
      "code": "violence",
      "name": "Violent",
      "adultOnly": true
    }
  ]
}
```
_Commentaire_ : pour les categories, il est possible de ne mentionner que l'id de celle-ci. Si une catégorie inexistante est mentionnée (id null ou inconnu), celle-ci ne sera pas sauvegardée.

### Exemple de réponse

```
{
  "id": "61dbc9a116781975a3d10dd1",
  "isbn": "9782070495023b",
  "title": "Total Kheops 3",
  "editor": "Folio",
  "numOfPages": 384,
  "publicationYear": 2012,
  "authors": [
    {
      "firstname": "Pierre-Henri",
      "lastname": "Dedieur"
    }
  ],
  "categories": [
    {
      "id": "61dbc9a116781975a3d10dcc",
      "code": "polar",
      "name": "Policier",
      "adultOnly": false
    },
    {
      "id": "61dbc9a116781975a3d10dcf",
      "code": "violence",
      "name": "Violent",
      "adultOnly": true
    }
  ],
  "copies": [
    {
      "id": "61dbc9a116781975a3d10dd5",
      "state": "GOOD",
      "removed": false,
      "available": true
    },
    {
      "id": "61dbc9a116781975a3d10dd6",
      "state": "GOOD",
      "removed": false,
      "available": true
    },
    {
      "id": "61dbc9a116781975a3d10dd7",
      "state": "USED",
      "removed": false,
      "available": true
    }
  ]
}
```

## GET /books/:bookId/loans

- __Récupère l'ensemble des prêts d'un livre pour toutes ses copies.__ Les prêts incluent la description du membre emprunteur et de la copie. les prêts sont retournés triés par date d'emprunt décroissante
- Paramètres d'URL :
  - _:bookId_ : l'identifiant du livre
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de livre inconnu
  - __500__ : Erreur serveur

### Exemple de réponse

```
[
  {
    "id": "61dcb7b2c51efa5f19491fdd",
    "member": {
      "id": "61dcb7b1c51efa5f19491fbb",
      "name": "Enstein",
      "firstname": "Albert",
      "birthday": "2011-11-01"
    },
    "bookCopy": {
      "id": "61dcb7b2c51efa5f19491fc6",
      "state": "GOOD",
      "removed": false,
      "available": false
    },
    "loanDateTime": "2022-01-03T09:23:00",
    "initialState": "GOOD",
    "returnDateTime": null,
    "returnState": null
  },
  {
    "id": "61dcb7b2c51efa5f19491fd6",
    "member": {
      "id": "61dcb7b1c51efa5f19491fb8",
      "name": "Planck",
      "firstname": "Max",
      "birthday": "1971-10-20"
    },
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
    "id": "61dcb7b2c51efa5f19491fda",
    "member": {
      "id": "61dcb7b1c51efa5f19491fbc",
      "name": "Curie",
      "firstname": "Marie",
      "birthday": "2013-07-13"
    },
    "bookCopy": {
      "id": "61dcb7b2c51efa5f19491fc7",
      "state": "GOOD",
      "removed": false,
      "available": true
    },
    "loanDateTime": "2021-04-24T15:15:00",
    "initialState": "NEW",
    "returnDateTime": "2021-05-20T15:15:00",
    "returnState": "GOOD"
  }
]
```

## POST /books/:bookId/copies

- __Créer de nouvelles copies d'un livre.__ Le traitement se fait par lot (pour créer plusieurs copie en une seule fois), via un ordre de création comprenant le nombre de copie à créer et optionnellement leur état initial. Si l'état initial n'est pas renseigné, celui-ci sera "NEW" par défaut. Retourne la liste des copies créées.
- Paramètres d'URL :
  - _:bookId_ : l'identifiant du livre
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide
  - __404__ : Identifiant de livre inconnu
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "numCopies": 3,
  "initialState": "VERY_GOOD"
}
```

### Exemple de réponse

```
[
  {
    "id": "61dc27e61791682569dd9499",
    "state": "VERY_GOOD",
    "removed": false,
    "available": true
  },
  {
    "id": "61dc27e61791682569dd949a",
    "state": "VERY_GOOD",
    "removed": false,
    "available": true
  },
  {
    "id": "61dc27e61791682569dd949b",
    "state": "VERY_GOOD",
    "removed": false,
    "available": true
  }
]
```

## GET /books/:bookId_/copies/:copyId_

- __Récupère les informations d'une copie avec ses prêts.__ Les prêts incluent le membre emprunteur.
- Paramètres d'URL :
  - _:bookId_ : l'identifiant du livre
  - _:copyId_ : l'identifiant de la copie
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de livre inconnu, ou identifiant de la copie inconnue dans le livre
  - __500__ : Erreur serveur

### Exemple de réponse

```
{
  "id": "61dcb7b2c51efa5f19491fc6",
  "state": "GOOD",
  "removed": false,
  "available": false,
  "loans": [
    {
      "id": "61dcb7b2c51efa5f19491fd6",
      "member": {
        "id": "61dcb7b1c51efa5f19491fb8",
        "name": "Planck",
        "firstname": "Max",
        "birthday": "1971-10-20"
      },
      "loanDateTime": "2021-06-20T10:48:00",
      "initialState": "NEW",
      "returnDateTime": "2021-06-30T10:48:00",
      "returnState": "GOOD"
    },
    {
      "id": "61dcb7b2c51efa5f19491fdd",
      "member": {
        "id": "61dcb7b1c51efa5f19491fbb",
        "name": "Enstein",
        "firstname": "Albert",
        "birthday": "2011-11-01"
      },
      "loanDateTime": "2022-01-03T09:23:00",
      "initialState": "GOOD",
      "returnDateTime": null,
      "returnState": null
    }
  ]
}
```

## PUT /books/:bookId_/copies/:copyId_

- __Modifie les informations d'une copie si celle si n'a pas un emprunt en cours.__ Ne modifie que les informations suivantes : state, removed. Si state ou removed n'est pas renseignée, l'attribut n'est pas mis à jour. Retourne les informations du livre mises à jour détaillées (incluant ses prêts mentionnant le membre emprunteur).
- Paramètres d'URL :
- _:bookId_ : l'identifiant du livre
- _:copyId_ : l'identifiant de la copie
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide
  - __409__ : La copie est en cours d'emprunt et ne peut être modifiée
  - __404__ : Identifiant de livre inconnu, ou identifiant de la copie inconnue dans le livre
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "id": "61dc37a28d81e10bbe5e0505",
  "removed": false,
  "state": "GOOD"
}
```

### Exemple de réponse

```
{
  "id": "61dc37a28d81e10bbe5e0505",
  "state": "GOOD",
  "removed": false,
  "available": true,
  "loans": [
    {
      "id": "61dcb7b2c51efa5f19491fda",
      "member": {
        "id": "61dcb7b1c51efa5f19491fbc",
        "name": "Curie",
        "firstname": "Marie",
        "birthday": "2013-07-13"
      },
      "loanDateTime": "2021-04-24T15:15:00",
      "initialState": "NEW",
      "returnDateTime": "2021-05-20T15:15:00",
      "returnState": "VERY_GOOD"
    }
  ]
}
```

## GET /books/:bookId/copies/:copyId/loans

- __Récupère les prêts d'une copie d'un livre.__ Les prêts incluent le membre emprunteur.
- Paramètres d'URL :
  - _:bookId_ : l'identifiant du livre
  - _:copyId_ : l'identifiant de la copie
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de livre inconnu, ou identifiant de la copie inconnue dans le livre
  - __500__ : Erreur serveur

### Exemple de réponse

```
[
  {
    "id": "61dcb7b2c51efa5f19491fda",
    "member": {
      "id": "61dcb7b1c51efa5f19491fbc",
      "name": "Curie",
      "firstname": "Marie",
      "birthday": "2013-07-13"
    },
    "loanDateTime": "2021-04-24T15:15:00",
    "initialState": "NEW",
    "returnDateTime": "2021-05-20T15:15:00",
    "returnState": "GOOD"
  }
]
```
