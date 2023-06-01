# API REST de gestion des prêts

Base d'URL : __/api/v1/rest/loans__

## GET /loans

- __Récupère la liste des prêts.__ Ne mentionne que leur id et date et état d'emprunt et de retour.
- Codes retour possibles :
  - __200__ : OK
  - __500__ : Erreur serveur

### Exemple de réponse

```
[
  {
    "id": "61dcb7b2c51efa5f19491fde",
    "loanDateTime": "2021-12-28T17:48:00",
    "initialState": "VERY_GOOD",
    "returnDateTime": null,
    "returnState": null
  },
  {
    "id": "61dcb7b2c51efa5f19491fd6",
    "loanDateTime": "2021-06-20T10:48:00",
    "initialState": "NEW",
    "returnDateTime": "2021-06-30T10:48:00",
    "returnState": "GOOD"
  }
]
```

## POST loans

- __Créer un nouveau prêt.__ À partir d'un ordre de prêt (corps de requête), créer un prêt si ce dernier est possible. L'ordre de prêt contient l'identifiant du membre emprunteur, de la copie de livre empruntée, et optionnellement un temps d'emprunt. L'état intial de la copie à l'emprunt est automatiquement apposée par le serveur à partir de l'état actuel de la copie. Si le temps n'est pas fournie, le temps actuel est utilisée comme temps d'emprunt. Lorsque le prêt est créé l'exemplaire du livre emprunté devient automatiquement non disponible (available = false). Retourne le prêt créé avec le détail de son membre et de l'exmplaire du livre emprunté.
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide ou date de prêt renseigné et anterieur à la date de rendu du dernier prêt si existant
  - __406__ : Prêt impossible (copie non dispo. / retirée ou membre mineur et copie pour adulte)
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "memberId": "61dc37a18d81e10bbe5e04fa",
  "bookCopyId": "61dc37a28d81e10bbe5e050d",
  "loanDateTime": "2022-01-05T10:15:30"
}
```

### Exemple de réponse

```
{
  "id": "61dc3dd88d81e10bbe5e051d",
  "member": {
    "id": "61dc37a18d81e10bbe5e04fa",
    "name": "Curie",
    "firstname": "Marie",
    "birthday": "2006-04-24"
  },
  "bookCopy": {
    "id": "61dc37a28d81e10bbe5e050d",
    "state": "NEW",
    "removed": false,
    "available": false
  },
  "loanDateTime": "2022-01-05T10:15:30",
  "initialState": "NEW",
  "returnDateTime": null,
  "returnState": null
}
```

## GET /loans/:id

- __Récupère un prêt avec le détail de son membre, de sa copie de livre incluant le livre lui-même.__
- Paramètres d'URL :
  - _:id_ : l'identifiant du prêt
- Codes retour possibles :
  - __200__ : OK
  - __404__ : Identifiant de prêt inconnu
  - __500__ : Erreur serveur

### Exemple de réponse

```
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
    "book": {
      "id": "61dcb7b1c51efa5f19491fc2",
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
          "id": "61dcb7b1c51efa5f19491fbd",
          "code": "polar",
          "name": "Policier",
          "adultOnly": false
        }
      ]
    },
    "state": "GOOD",
    "removed": false,
    "available": false
  },
  "loanDateTime": "2021-06-20T10:48:00",
  "initialState": "NEW",
  "returnDateTime": "2021-06-30T10:48:00",
  "returnState": "GOOD"
}
```

## PUT loans/:id

- __Modifie un prêt.__ Peut modifier le temps du prêt (loanDateTime) et/ou le temps et l'état de retour (returnDateTime et returnState). les règles de gestion sont à consulter dans le manuel avancé des règles de gestion d'un prêt. Cette modification peut changer l'état de la copie du livre ainsi que son indicateur de disponibilité selon le changement effectué.
- En-têtes de requête attendus :
  - _Content-Type_ : Application/json
- Paramètres d'URL :
  - _:id_ : l'identifiant du prêt
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Corps de requête invalide, ou précondition définie dans les règles de gestion non respectée
  - __404__ : Identifiant de prêt inconnu
  - __415__ : Média non supporté (mauvais header Content-Type ou manquant)
  - __500__ : Erreur serveur

### Exemple de corps de requête

```
{
  "id": "61dcb7b2c51efa5f19491fd6",
  "loanDateTime": "2021-06-13T11:23:00",
  "returnDateTime": "2021-06-29T10:00:00",
  "returnState": "USED"
}
```

### Exemple de réponse

```
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
    "book": {
      "id": "61dcb7b1c51efa5f19491fc2",
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
          "id": "61dcb7b1c51efa5f19491fbd",
          "code": "polar",
          "name": "Policier",
          "adultOnly": false
        }
      ]
    },
    "state": "USED",
    "removed": false,
    "available": false
  },
  "loanDateTime": "2021-06-13T11:23:00",
  "initialState": "NEW",
  "returnDateTime": "2021-06-29T10:00:00",
  "returnState": "USED"
}
```

## DELETE loans/:id

- __Supprime un prêt (uniquement si ce dernier est en cours).__ la copie du livre redevient disponible et son état et remis à l'état initial du prêt.
- Paramètres d'URL :
  - _:id_ : l'identifiant du prêt
- Aucune donnée de réponse attendue
- Codes retour possibles :
  - __200__ : OK
  - __400__ : Le prêt n'est plus en cours (le livre a été rendu)
  - __404__ : Identifiant de prêt inconnu
  - __500__ : Erreur serveur
