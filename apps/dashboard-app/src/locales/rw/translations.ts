// eslint-disable-next-line import/prefer-default-export
export const TRANSLATIONS_RW = {
    overview: {
        welcomeMessage: {
            welcomeBack: 'Murakaza neza',
        },
        newDeliveryCard: {
            title: 'Gera ku bakiriya ahantu hose!',
            subtitle: "Koresha Link ya Delivery niba mudafite aderesi y'umukiriya. Umukiriya arahita abona SMS irimo link imwereka uko ashyiramo aderesi ye.",
            newDelivery: 'Kohereza Ubutumwa',
            deliveryLink: "Link ya Delivery"
        },
        defaultContactModal: {
            selectBusinessLocation: 'Aho business iherereye',
        },
        twoHourConfirmationModal: {
            deliveryWillTakeUpTwoHours: 'Nyuma yo gusaba delivery, amasaha abiri ashobora gushira package itaragera kuyakira.',
            ok: 'Ok'
        }
    },
    auth: {
        welcomeMessage: {
            title: 'Gera ku bakiriya bawe ahantu hose muri Kigali!',
            subtitle: "Koresha Vanoma usabe delivery mu masegonda 10!",
        },
        signUpForm: {
            termsOfUsePrefix: "Nemeye",
            termsOfUseSuffix: "Terms of Service",
            alreadyHaveAccount: 'Musanzwe mukoresha Vanoma?',
            signInHere: 'Injirira hano',
            enterYourBusinessPhoneNumber: 'Nomero ya telephone ya business',
            enterBusinessName: 'Andika izina',
            businessName: "Izina ry'umucuruzi cg business",
            verificationCode: "Umubare w'ibanga",
            enterVerificationCodeSentTo: "Andika umubare w'ibanga woherejwe kuri ****",
            submit: 'Ohereza',
            complete: 'Complete',
            verify: 'Verify',
            signUp: 'Iyandikishe',
            phoneNumber: 'Nomero ya telephone',
        },
        signInForm: {
            DoNotHaveAccount: 'Ni ubwa mbere muje?',
            signUpHere: 'Iyandikishe hano',
            welcomeBackToVanoma: 'Murakaza neza kuri Vanoma!',
            signIn: 'Injira',
            verify: 'Verify',
            number: 'Nomero',
            phoneNumber: 'Nomero ya telephone',
            verificationCode: "Umubare w'ibanga",
            continue: 'Komeza',
            selectAccount: 'Hitamo konti',
            numberMultiAssociated: 'Nimero ya terefone yawe ifitanye isano na konti nyinshi',
        },
    },
    delivery: {
        newContact: {
            save: 'Save',
            addNewContact: 'Shyiramo contact nshya',
            name: 'Izina (niba rihari)',
            add: 'add',
            phoneNumber: 'Nomero ya telephone',
        },
        newAddress: {
            save: 'Save',
            new: 'New',
            houseNumber: "Nomero y'inzu (niba ihari)",
            addressName: "Izina ry'aderese",
        },
        addressSelector: {
            save: 'Save',
            use: 'Yikoreshe',
            saveAddressForFutureUse: 'Bika aderese uzongere kuyikoresha',
            default: 'Default',
            deliveryLink: "Link ya Delivery",
            preview: "Yirebe"
        },
        contactSelector: {
            contactNotFound: 'Contact ntabwo ibonetse',
            searchContacts: 'Shaka contacts',
            doNotHaveContactsYet: 'Nta contacts mufite',
            me: 'Njye',
            newContact: 'Contact nshya',
            use: "Yikoreshe",
        },
        package: {
            small: 'Buto',
            medium: 'Buringaniye',
            large: 'Bunini',
            packageSize: "Ingano",
            pickupInstructionsOptional: 'Amabwiriza yo gutora (niba ari ngombwa)',
            pickupInstructions: 'Amabwiriza yo kubufata',
            dropOffInstructionsOptional: 'Amabwiriza yo gutanga (niba ari ngombwa)',
            dropOffInstructions: 'Amabwiriza yaho bujya',
            selectPackageSize: 'Hitamo ingano'
        },
        stops: {
            from: 'Ahava delivery',
            to: 'Ahajya delivery',
        },
        stop: {
            next: 'Komeza',
            edit: 'Hindura',
            add: 'Ongeraho',
            remove: 'Yikureho',
        },
        from: {
            egFirstDoorOnTheRight: 'E.g: Igipangu cya mbere iburyo.',
        },
        to: {
            egRingTheDoorbell: 'E.g: Uvuze inzogera ku gipangu',
        },
        confirmOrderPlaceModal: {
            youWantToPlaceThisOrder: 'Murifuza delivery aka kanya?',
            confirm: 'Emeza',
        },
        time:{
            pickUpTime:'Igihe cyo kubifata',
            deliveryTime:'Igihe cyo kubitanga',
            soonEnough: 'Vuba',
            prompt: "Hitamo isaha",
            tomorrow: 'Ejo',
            today: "Uyu munsi"
        },
        payment: {
            order: 'Order',
            checkingThePrice: 'Turi kureba igiciro...',
            cantCheckThePrice: 'Kureba igiciro byanze. Ongera ugerageze.',
            waitingForYourPayment: 'Dutegereje message yo kwishyura',
            payment: 'Kwishyura',
            priceWithCurrency: 'Rwf {{price}}',
            paymentPhoneNumber: 'Nomero ya telephone yo kwishyura',
            pay: 'Ishyura',
            new: 'New',
            cancel: 'Guhagarika',
            amount: 'Umubare',
            discount: 'Ikigabanyo (delivery nyinshi)',
            total: 'Yose hamwe',
        },
        trackingLinkModal: {
            deliveryTrackingLink: 'Delivery tracking link',
            copiedToTheClipboard:'Yandukuwe kuri clipboard!',
            close: 'Funga'
        }
    },
    deliveries: {
        orders: {
            thereIsNoDeliveryToShowYet: 'Nta butumwa murohereza',
            collapseAll: 'Hisha byose',
            deliveries: 'Deliveries',
            active: 'Izatangiye',
            complete: 'Izarangiye',
            new: 'New',
            request: 'Izasabwe',
            pending: 'Izujujwe',
        },
        order: {
            notAssigned: 'Not assigned',
            noEventsYet: 'Nta kiraba kuri delivery',
            cancel: 'Hagarika delivery',
            trackingEvents: 'Ibyabaye kuri delivery',
            active: 'Yatangiye',
            complete: 'Yarangiye',
            request: 'Yasabwe',
            pending: 'Yujujwe',
            status: 'Status',
            trackingNumber: 'Kurikirana delivery',
            price: 'Igiciro',
            placedAt: 'Gutumiza byabaye',
            driver: 'Utwaye ubutumwa',
            priceWithCurrency: 'Rwf {{price}}',
            paid: 'Yishyuye',
            unpaid: 'Ntiyishyuye',
            partial: "Igice",
            noCharge: "Nta kiguzi",
            openedAt: "Igihe yafunguriwe",
            deliveryLink: "Link ya delivery",
            openDeliveryLink: "link",
            copiedToTheClipboard: 'Yandukuwe kuri clipboard!',
            customer: "Umukiriya",
            branch: 'Ishami'
        },
        request: {
            requestFor: 'Delivery ya {{customer}}',
            sent: 'SMS',
            deliveryLinkSent: 'SMS irimo link ya delivery yoherejwe.',
            opened: 'Yafunguwe',
            yetToBeOpened: 'Umurukiriya ntabwo arafungura link delivery.',
            customerOpenedLink: 'Umukiriya yafunguye link ya delivery.',
            address: 'Aderesi',
            yetToProvideAddress: 'Umukiriya ntabwo aratanga aderesi ya delivery.',
            customerProvidedAddress: 'Umukiriya yatanze aderesi ya delivery.',
            payment: 'Kwishyura',
            yetToBePaid: 'Delivery ntabwo irishyurwa.',
            dispatched: 'Umu rider',
            yetToBeDispatched: 'Umu rider ntabwo arahaguruka.',
            waitingForCustomerToPay: 'Dutegereje ko umukiriya yishyura Rwf {{price}}',
            pickUp: 'Kuva',
            dropOff: 'Kujya'
        },
        cancelDeliveryModal: {
            cancelDelivery: 'Hagarika delivery',
            reason: 'Impamvu',
            cancel: 'Guhagarika',
        },
        events: {
            deliveryRequested: 'Delivery irasabwe',
            driverAssigned: 'Rider aremenyeshejwe',
            goingToPickUp: 'Kujya gufata ubutumwa',
            pickUpArrival: 'Rider ageze ahari ubutumwa',
            packagePickedUp: 'Ubutumwa bwafashwe',
            goingToDropOff: 'Kujya gutanga ubutumwa',
            dropOffArrival: 'Kugera ahatangwa ubutumwa',
            packageDelivered: 'Ubutumwa bwatanzwe',
            packageCancelled: 'Delivery yahagaritswe'
        },
    },
    customers: {
        deleteAddressModal: {
            deleteThisAddress: 'Siba iyi address',
            yes: 'Yego',
            no: 'Oya',
        },
        editContactModal: {
            editContact: 'Hindura iyi contact',
        },
        contacts: {
            contactNotFound: 'Contact ntabwo abonetse',
            searchContacts: 'Shaka contacts',
            customers: 'Contacts',
            newCustomer: 'Umukiriya mushya',
        },
        contact: {
            delete: 'Siba',
            edit: 'Hindura',
            link: 'Link',
            noAddress: 'Umukiriya nta aderesi afite',
        },
        linkMessageModal: {
            getDeliveryLink: "Ushaka Link ya Delivery?",
            yes: 'Yego',
            no: 'Oya'
        },
        linkGeneratorModal: {
            deliveryLinkSent: "Link ya Delivery yoherejwe ",
            whoIsPaying: 'Ninde wishyura delivery?',
            copiedToTheClipboard: 'Yandukuwe kuri clipboard!',
            close: 'Funga',
            me: 'Njye',
            customer: 'Umukiriya',
            getDeliveryLink: "Ohereza Link ya Delivery",
            customerPhoneNumber: "Nomero y'umukiriya",
            pickupInstructionsOptional: 'Amabwiriza (niba akenewe)',
            more: 'Amabwiriza'
        },
        deleteContactModal: {
            deleteThisContact: 'Siba iyi contact',
            yes: 'Yego',
            no: 'Oya',
        },
    },
    billing: {
        payments: {
            billing: 'Kwishyura',
            payBalance: 'Facture',
            paymentMethods: 'Uburyo bwo kwishyura',
        },
        payBalance: {
            unpaidDeliveries: 'Deliveries zitishyuwe',
            allUnpaid: 'Zose',
            onlyUntil: 'Kugeza',
            deliveries: 'Deliveries',
            cost: 'Igiciro',
            transactionFee: 'Transaction fee',
            totalAmount: 'Igiciro',
            paymentMethod: 'Telephone yishyura',
            filterByBranch: 'Hitamo ishami',
            allBranches: 'Amashami yose'
        },
        paymentMethods: {
            new: 'New',
            default: 'Default',
            remove: 'Remove',
        },
        newPaymentMethodModal: {
            addNewPaymentMethod: 'Uburyo bwo kwishyura',
            phoneNumber: 'Telephone',
            momoCodeOptional: 'Momo code (niba ihari)',
            add: 'Add',
        },
        removePaymentMethodModal: {
            removeThisPaymentMethod: 'Siba ubu buryo bwo kwishyura?',
            yes: 'Yego',
            no: 'Oya',
        },
        selectPaymentMethod: {
            newPaymentMethod: 'Indi MoMo',
            paymentMethodNotFound: 'Telephone yishyura ntabwo bwabonetse',
        },
        paymentDueAlert: {
            paymentRequired: 'Mukeneye kwishyura deliveries zarangiye',
            pay: "Ishyura",
        },
    },
    alertAndValidationMessages: {
        pleaseCheckYourInternetConnection: 'Murebe ko mufite internet.',
        somethingWentWrong: 'Hari ibitagenze neza. Hamagara customer support.',
        paymentReceivedSuccessful: 'Kwishyura bigenze neza.',
        ourDriverWillBeThereSoon: 'Tugiye kohereza utwara ubutumwa.',
        checkYourPhoneForPayment: 'Emeza ayo kwishyura kuri telephone.',
        orderReceivedSuccessful: 'Commande yanyu itugezeho. Tugiye kohereza utwara ubutumwa.',
        verificationCodeRequired: "Shyiramo umubare w'ibanga.",
        verificationCodeMustBeNumber: "Umubare w'ibanga ni imibare gusa.",
        verificationCodeValid: "Umubare w'ibanga ugizwe n'imibare itandatu.",
        termsOfUseRequired: 'Mugomba kwemera Terms of Use.',
        firstNameRequired: 'Mushyiremo izina rya mbere ryanyu.',
        lastNameRequired: 'Mushyiremo izina rya kabiri ryanyu.',
        businessNameRequired: 'Mushyiremo izina rya business.',
        addressNameRequired: 'Mushyiremo izina rya aderese.',
        reasonCancelledDeliveryRequired: 'Mushyiremo impamvu yo guhagarika delivery.',
        momoCodeMustBeNumber: 'MoMo code igomba kuba umabare.',
        momoCodeMustHaveValidLength: 'MoMo code igira imibare 5 cg 6.',
        paymentMethodRequired: 'Muhitemo uburyo bwo kwishyura.',
        signInAndTryAgain: 'Please sign in and try again.',
        dontHaveCustomerAccount: "You don't have customer account. Please create one",
        phoneNumberRequired: 'Uzuza nomero ya telephone',
        phoneNumberMustBeNumber: 'Nomero ya telephone ni imibare gusa.',
        phoneNumberValid: 'Nomero ya telephone ifite ikibazo.',
        houseNumberNotValid: "Inomero yinzu ntiyemewe",
        branchNameRequired: "Andika izina ry'ishami",
        branchContactRequired: "Andika contact y'ishami",
        branchAddressRequired: "Andika aderese y'ishami",
        agentNameRequired: "Andika amazina y'umukozi",
        branchRequired: 'Hitamo ishami',
        accountRequired: 'Hitamo konti'
    },
    dashboard: {
        navbar: {
            signOut: 'Sign out',
            languages: 'Indimi',
            turnDarkModeOn: 'Turn dark mode on',
            turnDarkModeOff: 'Turn dark mode off',
            thereIsNoNotifications: 'Nta notifications zihari',
            new: 'Izitarasomwa',
            viewed : 'Izasomwe',
            markAllAsRead: 'Imeza ko zasomwe zose',
            notifications:'Notifications',
            unreadNotifications:'Mufite notifications {{totalUnread}} zitarasomwa'
        },
        sidebar: {
            customers: 'Contacts',
            business: 'Business',
            deliveries: 'Deliveries',
            newDelivery: 'Kohereza Ubutumwa',
            overview: 'Ahabanza',
            billing: 'Kwishyura',
            account: 'Konti'
        },
    },
    application: {
        exception: {
            message: "Hari ikintu gitunguranye cyibaye kandi ikibazo cyamenyeshejwe ikipe yacu.",
            callout: "Gerageza gusubiramo page ukanze iyi buto hepfo. Niba ikibazo gikomeje, wavugana nitsinda ryacu ryunganira abakiriya.",
            refresh: "Reload page"
        }
    },
    selectPaymentMethod: {
        searchPaymentMethods: 'Shaka telephone zishyura',
        notfound: 'Telephone yishyura ntabwo ibonetse',
        new: 'New',
    },
    account: {
        main: {
            account: 'Konti',
            branches: 'Amashami',
            agents: 'Abakozi',
        },
        branches: {
            branches: 'Amashami',
            newBranch: 'Ishami rishya',
            delete: 'Siba',
            edit: 'Hindura',
            branchesNotFound: "Nta mashami ufite",
        },
        newBranchModal: {
            branchName: "Izina ry'ishami",
            addNewBranch: 'Shyiramo ishami rishya',
            editBranch: 'Hindura iri shami',
            add: 'Shyiramo',
            edit: 'Hindura',
        },
        deleteBranchModal: {
            deleteThisBranch: 'Siba iri shami',
            yes: 'Yego',
            no: 'Oya',
        },
        agents: {
            agents: 'Abakozi',
            newAgent: 'Umukozi mushya',
            delete: 'Siba',
            edit: 'Hindura',
            agentsNotFound: 'Nta mukozi ufite',
            main: 'Nyamukuru'
        },
        newAgentModal: {
            addNewAgent: 'Shyiramo umukozi mushya',
            fullName: 'Amazina yose',
            phoneNumber: 'Nomero ya telephone',
            editAgent: 'Hindura umukozi',
            add: 'Shyiramo',
            edit: 'Hindura',
            selectBranch: 'Hitamo ishami'

        },
        deleteAgentModal: {
            deleteThisAgent: 'Siba uyu mukozi',
            yes: 'Yego',
            no: 'Oya',
        },
    }
};
