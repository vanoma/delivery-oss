// eslint-disable-next-line import/prefer-default-export
export const TRANSLATIONS_FR = {
    overview: {
        welcomeMessage: {
            welcomeBack: 'Bon retour',
        },
        newDeliveryCard: {
            title: "Atteignez vos clients n'importe où!",
            subtitle: "Utilisez Lien de Livraison si vous ne connaissez pas l'adresse de livraison du client. Le client recevra un SMS avec un lien pour préciser son adresse.",
            newDelivery: 'Nouvelle livraison',
            deliveryLink: "Lien de Livraison"
        },
        defaultContactModal: {
            selectBusinessLocation: 'Adresse de votre entreprise',
        },
        twoHourConfirmationModal: {
            deliveryWillTakeUpTwoHours: 'Après avoir passé la commande, le colis sera livré dans un délai maximum de 2 heures.',
            ok: 'Ok'
        }
    },
    auth: {
        welcomeMessage: {
            title: 'Atteignez vos clients partout à Kigali!',
            subtitle: 'Avec Vanoma, vous pouvez demander une livraison en 10 secondes',
        },
        signUpForm: {
            termsOfUsePrefix: "J'accepte les",
            termsOfUseSuffix: "Conditions d'Utilisation",
            alreadyHaveAccount: 'Déjà inscrit(e)?',
            signInHere: 'Identifiez-vous',
            enterYourBusinessPhoneNumber: "Numéro de téléphone de l'entreprise",
            enterBusinessName: "Entrez le nom",
            businessName: "Nom du vendeur ou de l'entreprise",
            verificationCode: 'Code de vérification',
            enterVerificationCodeSentTo: 'Entrez le code de vérification envoyé à ****',
            submit: 'Soumettre',
            complete: 'Terminer',
            signUp: "S'inscrire",
            verify: 'Vérifier',
            phoneNumber: 'Téléphone',
        },
        signInForm: {
            DoNotHaveAccount: 'Nouveau sur Vanoma?',
            signUpHere: 'Créer votre compte',
            welcomeBackToVanoma: 'Bienvenue à Vanoma!',
            signIn: 'Identifiez-vous',
            verify: 'Vérifier',
            number: 'Téléphone',
            phoneNumber: 'Téléphone',
            verificationCode: 'Code de vérification',
            continue: 'Continuer',
            selectAccount: 'Sélectionnez un compte',
            numberMultiAssociated: 'Votre numéro de téléphone est associé à plusieurs comptes',
        },
    },
    delivery: {
        newContact: {
            save: 'Enregistrer',
            addNewContact: 'Ajouter un nouveau contact',
            name: 'Nom (optionnel)',
            add: 'Ajouter',
            phoneNumber: 'Téléphone',
        },
        newAddress: {
            save: 'Enregistrer',
            new: 'Nouvelle',
            houseNumber: 'Numéro de maison (optionnel)',
            addressName: "Nom de l'adresse",
        },
        addressSelector: {
            save: 'Enregistrer',
            use: 'Utiliser',
            saveAddressForFutureUse: 'Enregistrer cette adresse',
            default: 'Par défaut',
            deliveryLink: "Lien de Livraison",
            preview: "Aperçu"
        },
        contactSelector: {
            contactNotFound: 'Le contact est introuvable',
            searchContacts: 'Recherche',
            doNotHaveContactsYet: "Vous n'avez aucun contact.",
            me: 'Moi',
            newContact: 'Nouveau contact',
            use: "utiliser",
        },
        package: {
            small: 'Petit',
            medium: 'Moyen',
            large: 'Large',
            packageSize: 'Taille',
            pickupInstructionsOptional: 'Instructions de ramassage (optionnel)',
            pickupInstructions: 'Instructions de ramassage',
            dropOffInstructionsOptional: 'Instructions de dépôt (optionnel)',
            dropOffInstructions: 'Instructions de dépôt',
            selectPackageSize: 'Sélectionnez la taille du paquet'
        },
        stops: {
            from: 'Origine',
            to: 'Destination',
        },
        stop: {
            next: 'Suivant',
            edit: 'Modifier',
            add: 'Ajouter',
            remove: 'Éliminer',
        },
        from: {
            egFirstDoorOnTheRight: 'E.g: Première maison à droite.',
        },
        to: {
            egRingTheDoorbell: 'E.g: Sonner à la porte',
        },
        confirmOrderPlaceModal: {
            youWantToPlaceThisOrder: 'Passer cette commande?',
            confirm: 'Confirmer',
        },
        time:{
            pickUpTime:'Heure de ramassage',
            deliveryTime:'Heure de livraison',
            soonEnough: 'Vite',
            prompt: "Sélectionner l'heure",
            tomorrow: 'Demain',
            today: "Aujourd'hui"
        },
        payment: {
            order: 'Commander',
            checkingThePrice: 'Calcul du prix...',
            cantCheckThePrice: 'Impossible de trouver le prix. Veuillez réessayer.',
            waitingForYourPayment: 'En attente de votre paiement',
            payment: 'Paiement',
            priceWithCurrency: 'Rwf {{price}}',
            paymentPhoneNumber: 'Numéro de téléphone pour payer',
            pay: 'Payer',
            new: 'Nouveau',
            cancel: 'Annuler',
            amount: 'Montant',
            discount: 'Remise (plusieurs livraisons)',
            total: 'Total',
        },
        trackingLinkModal: {
            deliveryTrackingLink: 'Lien de suivi de livraison',
            copiedToTheClipboard:'Copié dans le presse-papier!',
            close: 'Fermer'
        }
    },
    deliveries: {
        orders: {
            thereIsNoDeliveryToShowYet: "Vous n'avez pas encore de livraisons",
            collapseAll: 'Cacher tout',
            deliveries: 'Livraisons',
            active: 'Active',
            complete: 'Terminé',
            new: 'Nouvelle',
            request: 'Demande',
            pending: 'En attente',
        },
        order: {
            notAssigned: 'Pas encore attribuée',
            noEventsYet: 'Aucun événement pour le moment',
            cancel: 'Annuler la livraison',
            trackingEvents: 'Événements de livraison',
            active: 'Active',
            complete: 'terminé',
            request: 'Demande',
            pending: 'En attente',
            status: 'Statut',
            trackingNumber: 'Suivi la livraison',
            price: 'Prix',
            placedAt: 'commandé à',
            driver: 'Chauffeur',
            priceWithCurrency: 'Rwf {{price}}',
            paid: 'Payé',
            unpaid: 'Non payé',
            partial: "Partiel",
            noCharge: "Pas de prix",
            openedAt: "Ouvert à",
            deliveryLink: "Lien de Livraison",
            openDeliveryLink: "lien",
            copiedToTheClipboard: 'Copié dans le presse-papier!',
            customer: 'Client',
            branch: 'Branche'
        },
        request: {
            requestFor: 'Livraison pour {{customer}}',
            sent: 'Envoyé',
            deliveryLinkSent: 'SMS avec le lien de livraison a été envoyé.',
            opened: 'Ouvert',
            yetToBeOpened: "Le client n'a pas encore ouvert le lien de livraison.",
            customerOpenedLink: 'Le client a ouvert le lien de livraison.',
            address: 'Adresse',
            yetToProvideAddress: "Le client n'a pas encore donné l'adresse de livraison.",
            customerProvidedAddress: "Le client a spécifié l'adresse de livraison.",
            payment: 'Paiement',
            yetToBePaid: 'Les frais de livraison restent à payer.',
            dispatched: 'Chauffeur',
            yetToBeDispatched: "Le chauffeur n'a pas encore été envoyé.",
            waitingForCustomerToPay: 'Nous attendons que le client paie Rwf {{price}}',
            pickUp: 'Origine',
            dropOff: 'Destination'
        },
        cancelDeliveryModal: {
            cancelDelivery: 'Annuler la livraison',
            reason: 'Raison',
            cancel: 'Annuler',
        },
        events: {
            deliveryRequested: 'Livraison demandée',
            driverAssigned: 'Chauffeur affecté',
            goingToPickUp: 'Vers ramassage',
            pickUpArrival: 'Arrivé au ramassage',
            packagePickedUp: 'Colis ramassé',
            goingToDropOff: 'Vers à destination',
            dropOffArrival: 'Arrivé à destination',
            packageDelivered: 'Colis livré',
            packageCancelled: 'La livraison a été annulée',
        },
    },
    customers: {
        deleteAddressModal: {
            deleteThisAddress: 'Effacer cette adresse',
            yes: 'Oui',
            no: 'No',
        },
        editContactModal: {
            editContact: 'Modifier le contact',
        },
        contacts: {
            contactNotFound: 'Le contact est introuvable',
            searchContacts: 'Recherche',
            customers: 'Clients',
            newCustomer: 'Nouveau client'
        },
        contact: {
            delete: 'Effacer',
            edit: 'Modifier',
            link: 'Lien',
            noAddress: "Le client n'a pas encore d'adresse",
        },
        linkMessageModal: {
            getDeliveryLink: 'Envoyer le Lien de Livraison?',
            yes: 'Oui',
            no: 'Non'
        },
        linkGeneratorModal: {
            deliveryLinkSent: 'Lien client envoyé',
            whoIsPaying: 'Qui paie la livraison?',
            copiedToTheClipboard: 'Copié dans le presse-papier!',
            close: 'Fermer',
            me: 'Moi',
            customer: 'Client',
            getDeliveryLink: 'Envoyer le Lien de Livraison',
            customerPhoneNumber: 'Numéro du client',
            pickupInstructionsOptional: 'Instructions (optionnel)',
            more: 'Instructions'
        },
        deleteContactModal: {
            deleteThisContact: 'Effacer ce contact',
            yes: 'Oui',
            no: 'No',
        },
    },
    billing: {
        payments: {
            billing: 'Facturation',
            payBalance: 'Paiements',
            paymentMethods: 'Modes de paiement',
        },
        payBalance: {
            unpaidDeliveries: 'Paiements dus',
            allUnpaid: 'Tous non payés',
            onlyUntil: "Jusqu'à",
            deliveries: 'Livraisons',
            totalAmount: 'Coût total',
            cost: 'Prix',
            transactionFee: 'Frais de transaction',
            paymentMethod: 'Numéros de téléphone de paiement',
            filterByBranch: 'Filtrer par branche',
            allBranches: 'Toutes les branches'
        },
        paymentMethods: {
            new: 'Nouveau',
            default: 'Par défaut',
            remove: 'Effacer',
        },
        newPaymentMethodModal: {
            addNewPaymentMethod: 'Nouveau mode de paiement',
            phoneNumber: 'Téléphone',
            momoCodeOptional: 'Momo code (optionnel)',
            add: 'Ajouter',
        },
        removePaymentMethodModal: {
            removeThisPaymentMethod: 'Effacer ce mode de paiement?',
            yes: 'Oui',
            no: 'No',
        },
        selectPaymentMethod: {
            newPaymentMethod: 'Nouveau numéro de téléphone de paiement',
            paymentMethodNotFound: 'Numéro de téléphone de paiement introuvable',
        },
        paymentDueAlert: {
            paymentRequired: 'Le paiement est dû pour les livraisons terminées',
            pay: "Payer",
        },
    },
    alertAndValidationMessages: {
        pleaseCheckYourInternetConnection: 'Vérifiez votre connection internet.',
        somethingWentWrong: "Quelque chose s'est mal passé. Veuillez contacter l'assistance.",
        paymentReceivedSuccessful: 'Paiement reçu avec succès.',
        ourDriverWillBeThereSoon: 'Notre chauffeur sera là bientôt.',
        checkYourPhoneForPayment: 'Confirmez le paiement sur votre téléphone.',
        orderReceivedSuccessful: 'Commande reçu avec succès. Notre chauffeur sera là bientôt.',
        verificationCodeRequired: 'Veuillez entrer le code de vérification.',
        verificationCodeMustBeNumber: 'Le code de vérification doit être un nombre.',
        verificationCodeValid: 'Le code de vérification doit être avoir 6 chiffres.',
        termsOfUseRequired: "Vous devez accepter les Conditions d'Utilisation.",
        firstNameRequired: 'Veuillez entrer votre prénom.',
        lastNameRequired: 'Veuillez Entrer votre now de famille.',
        businessNameRequired: "Veuillez le nom de l'entreprise.",
        addressNameRequired: "Veuillez entre le nom de l'adresse.",
        reasonCancelledDeliveryRequired: "Veuillez écrire le motif de l'annulation.",
        momoCodeMustBeNumber: 'MoMo code doit être numérique.',
        momoCodeMustHaveValidLength: 'Momo code must be 5 or 6 numbers',
        paymentMethodRequired: 'Veuillez choisir un mode de paiement.',
        signInAndTryAgain: 'Please sign in and try again.',
        dontHaveCustomerAccount: "You don't have customer account. Please create one",
        phoneNumberRequired: 'Veuillez entrer le numéro de téléphone.',
        phoneNumberMustBeNumber: 'Le numéro de téléphone doit être numérique',
        phoneNumberValid: 'PLe numéro de téléphone est invalide.',
        houseNumberNotValid: "Le numéro de maison n'est pas valide",
        branchNameRequired: 'Veuillez entrer le nom de la branche',
        branchContactRequired: 'Veuillez entrer le contact de la branche',
        branchAddressRequired: "Veuillez entrer l'adresse de la branche",
        agentNameRequired: 'Veuillez entrer les noms des agents',
        branchRequired: 'Veuillez sélectionner la branche',
        accountRequired: 'Veuillez sélectionner un compte'
    },
    dashboard: {
        navbar: {
            signOut: 'Déconnexion',
            languages: 'Langues',
            turnDarkModeOn: 'Activer le mode sombre',
            turnDarkModeOff: 'Désactiver le mode sombre',
            thereIsNoNotifications: "Il n'y a aucune notification",
            new: 'Nouvelle(s)',
            viewed : 'Vue(s)',
            markAllAsRead: 'Marquer tout comme vu',
            notifications:'Notifications',
            unreadNotifications:'Vous avez {{totalUnread}} notifications non lues'
        },
        sidebar: {
            customers: 'Clients',
            deliveries: 'livraisons',
            newDelivery: 'Nouvelle livraison',
            overview: 'Accueil',
            billing: 'Facturation',
            account: 'Compte',
        },
    },
    application: {
        exception: {
            message: "Quelque chose d'inattendu s'est produit et le problème a été signalé à notre équipe.",
            callout: "Essayez d'actualiser la page en cliquant sur le bouton ci-dessous. Si le problème persiste, veuillez contacter notre service client.",
            refresh: "Rafraîchir la page"
        }
    },
    selectPaymentMethod: {
        searchPaymentMethods: 'Trouver des numéros de téléphone de paiement',
        notfound: 'Numéro de téléphone de paiement introuvable',
        new: 'Nouveau',
    },
    account: {
        main: {
            account: 'Compte',
            branches: 'Branches',
            agents: 'Agents',
        },
        branches: {
            branches: 'Branches',
            newBranch: 'Nouveau branche',
            delete: 'Effacer',
            edit: 'Modifier',
            branchesNotFound: "Il n'y a pas de branche"
        },
        newBranchModal: {
            branchName: 'Non de la branche',
            addNewBranch: 'Ajouter une nouvelle branche',
            editBranch: 'Modifier la branche',
            add: 'Ajouter',
            edit: 'Modifier',
        },
        deleteBranchModal: {
            deleteThisBranch: 'Effacer cette branche',
            yes: 'Oui',
            no: 'No',
        },
        agents: {
            agents: 'Agents',
            newAgent: 'Nouveau agent',
            delete: 'Effacer',
            edit: 'Modifier',
            agentsNotFound: "Il n'y a pas d'agents",
            main: 'Principal'
        },
        newAgentModal: {
            addNewAgent: 'Ajouter un nouvel agent',
            fullName: 'Nom et prénom',
            phoneNumber: 'Téléphone',
            editAgent: "Modifier l'agent",
            add: 'Ajouter',
            edit: 'Modifier',
            selectBranch: 'Sélectionner la branche'
        },
        deleteAgentModal: {
            deleteThisAgent: 'Supprimer cet agent',
            yes: 'Oui',
            no: 'No',
        },
        
    }
};
