# ADR-004 — Communication REST, idempotence et gestion des défaillances

**Statut** : Accepté

## Contexte
Les services communiquent de façon synchrone. Commandes et activations peuvent être rejouées après une erreur réseau ou un délai dépassé.

## Décision
REST synchrone uniquement (aucun courtier de messages en Phase 2). Commandes : idempotence par (clientId, Idempotency-Key) avec empreinte du contenu; activations : serviceId comme clé. Même clé et même contenu = même ressource; contenu différent = 409. Erreurs en application/problem+json (RFC 9457). Délais d'attente et disjoncteur Resilience4j.

## Conséquences
Aucun doublon de commande, de service ou de configuration réseau. Complexité fonctionnelle supplémentaire.

## Option écartée
Transactions distribuées entre microservices.
