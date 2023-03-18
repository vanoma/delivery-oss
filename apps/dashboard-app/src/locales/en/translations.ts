// eslint-disable-next-line import/prefer-default-export
export const TRANSLATIONS_EN = {
    overview: {
        welcomeMessage: {
            welcomeBack: 'Welcome back',
        },
        newDeliveryCard: {
            title: 'Reach customers anywhere!',
            subtitle: "Use Delivery Link if you do not know the customer's delivery address. The customer will receive an SMS with a link to specify their address.",
            newDelivery: 'New delivery',
            deliveryLink: "Delivery Link"
        },
        defaultContactModal: {
            selectBusinessLocation: 'Select business location',
        },
        twoHourConfirmationModal: {
            deliveryWillTakeUpTwoHours: 'After placing the order, the package will be delivered in not more than 2 hours.',
            ok: 'Ok'
        }
    },
    auth: {
        welcomeMessage: {
            title: 'Reach your customers everywhere in Kigali',
            subtitle: 'With Vanoma, you can request a delivery in 10 seconds!',
        },
        signUpForm: {
            termsOfUsePrefix: 'I accept the',
            termsOfUseSuffix: 'Terms of Service',
            alreadyHaveAccount: 'Already have an account?',
            signInHere: 'Sign in here',
            enterYourBusinessPhoneNumber: 'Enter your business phone number',
            enterBusinessName: 'Enter the name',
            businessName: 'Seller or business name',
            verificationCode: 'Verification code',
            enterVerificationCodeSentTo: 'Enter verification code sent to ****',
            submit: 'Submit',
            complete: 'Complete',
            verify: 'Verify',
            signUp: 'Sign up',
            phoneNumber: 'Phone number',
        },
        signInForm: {
            DoNotHaveAccount: "Don't have an account?",
            signUpHere: 'Sign up here',
            welcomeBackToVanoma: 'Welcome back to Vanoma!',
            signIn: 'Sign in',
            verify: 'Verify',
            number: 'Number',
            phoneNumber: 'Phone number',
            verificationCode: 'Verification code',
            continue: 'Continue',
            selectAccount: 'Select account',
            numberMultiAssociated: 'You phone number is associated with multiple accounts',

        },
    },
    delivery: {
        newContact: {
            save: 'Save',
            addNewContact: 'Add new contact',
            name: 'Name (optional)',
            add: 'add',
            phoneNumber: 'Phone number',
        },
        newAddress: {
            save: 'Save',
            new: 'New',
            houseNumber: 'House number (Optional)',
            addressName: 'Address name',
        },
        addressSelector: {
            save: 'Save',
            use: 'Use',
            saveAddressForFutureUse: 'Save address for future use',
            default: 'Default',
            deliveryLink: "Delivery Link",
            preview: "Preview"
        },
        contactSelector: {
            contactNotFound: 'Contact not found',
            searchContacts: 'Search contacts',
            doNotHaveContactsYet: "You don't have any contact yet.",
            me: 'Me',
            newContact: 'New Contact',
            use: "use",
        },
        package: {
            small: 'Small',
            medium: 'Medium',
            large: 'Large',
            packageSize: 'Size',
            pickupInstructionsOptional: 'Pickup instructions (Optional)',
            pickupInstructions: 'Pickup instructions',
            dropOffInstructionsOptional: 'Drop-off instructions (Optional)',
            dropOffInstructions: 'Drop-off instructions',
            selectPackageSize: 'Select the package size'
        },
        stops: {
            from: 'Pickup From',
            to: 'Deliver To',
        },
        stop: {
            next: 'Next',
            edit: 'Edit',
            add: 'Add',
            remove: 'Remove',
        },
        from: {
            egFirstDoorOnTheRight: 'E.g: First door on the right',
        },
        to: {
            egRingTheDoorbell: 'E.g: Ring the doorbell',
        },
        confirmOrderPlaceModal: {
            youWantToPlaceThisOrder: 'Place this order?',
            confirm: 'Confirm',
        },
        time:{
            pickUpTime:'Pickup Time',
            deliveryTime:'Delivery Time',
            soonEnough:'Soon enough',
            prompt: 'Select Time',
            tomorrow: 'Tomorrow',
            today: 'Today'
        },
        payment: {
            order: 'Order',
            checkingThePrice: 'Checking the price...',
            cantCheckThePrice: "Can't check the price. Please try again.",
            waitingForYourPayment: 'Waiting for your payment',
            payment: 'Payment',
            priceWithCurrency: 'Rwf {{price}}',
            paymentPhoneNumber: 'Payment phone number',
            pay: 'Pay',
            cancel: 'Cancel',
            amount: 'Amount',
            discount: 'Discount (multiple deliveries)',
            total: 'Total',
        },
        trackingLinkModal: {        
            deliveryTrackingLink: 'Delivery tracking link',
            copiedToTheClipboard: 'Copied to the clipboard!',
            close: 'Close'
        }
    },
    deliveries: {
        orders: {
            thereIsNoDeliveryToShowYet: 'There is no delivery to show yet.',
            collapseAll: 'Hide all',
            deliveries: 'Deliveries',
            active: 'Active',
            complete: 'complete',
            new: 'New',
            request: 'Request',
            pending: 'Pending',
        },
        order: {
            notAssigned: 'Not assigned',
            noEventsYet: 'No events yet',
            cancel: 'Cancel delivery',
            trackingEvents: 'Delivery events',
            active: 'Active',
            complete: 'complete',
            request: 'Request',
            pending: 'Pending',
            status: 'Status',
            trackingNumber: 'Track delivery',
            price: 'Price',
            placedAt: 'Placed at',
            driver: 'Driver',
            priceWithCurrency: 'Rwf {{price}}',
            paid: 'Paid',
            unpaid: 'Unpaid',
            partial: "Partial",
            noCharge: "No charge",
            openedAt: "Opened at",
            deliveryLink: "Delivery Link",
            openDeliveryLink: "link",
            copiedToTheClipboard: 'Copied to the clipboard!',
            customer: 'Customer',
            branch: 'Branch'
        },
        request: {
            requestFor: 'Request for {{customer}}',
            sent: 'Sent',
            deliveryLinkSent: 'SMS with delivery link sent.',
            opened: 'Opened',
            yetToBeOpened: 'Customer is yet to open the delivery link.',
            customerOpenedLink: 'Customer opened the delivery link.',
            address: 'Address',
            yetToProvideAddress: 'Customer is yet to provide the delivery address.',
            customerProvidedAddress: 'Customer provided the delivery address.',
            payment: 'Payment',
            yetToBePaid: 'The delivery fee is yet to be paid.',
            dispatched: 'Dispatched',
            yetToBeDispatched: 'Delivery driver is yet to be dispatched.',
            waitingForCustomerToPay: 'We are waiting for the customer to pay Rwf {{price}}',
            pickUp: 'Pick-up',
            dropOff: 'Drop-off'
        },
        cancelDeliveryModal: {
            cancelDelivery: 'Cancel delivery',
            reason: 'Reason',
            cancel: 'cancel',
        },
        events: {
            deliveryRequested: 'Delivery requested',
            driverAssigned: 'Driver assigned',
            goingToPickUp: 'Going to pick-up',
            pickUpArrival: 'Arrived at pick-up',
            packagePickedUp: 'Package picked Up',
            goingToDropOff: 'Going to drop-off',
            dropOffArrival: 'Arrived at drop-off',
            packageDelivered: 'Package delivered',
            packageCancelled: 'Delivery has been cancelled'
        },
    },
    customers: {
        deleteAddressModal: {
            deleteThisAddress: 'Delete this address',
            yes: 'Yes',
            no: 'No',
        },
        editContactModal: {
            editContact: 'Edit contact',
        },
        contacts: {
            contactNotFound: 'Contact not found',
            searchContacts: 'Search customers',
            customers: 'Customers',
            newCustomer: 'New customer'
        },
        contact: {
            delete: 'Delete',
            edit: 'edit',
            link: 'Link',
            noAddress: 'Customer has no address yet',
        },
        linkMessageModal: {
            getDeliveryLink: 'Send Delivery Link?',
            yes: 'Yes',
            no: 'No'
        },
        linkGeneratorModal: {
            deliveryLinkSent: 'Delivery Link sent',
            whoIsPaying: 'Who is paying for delivery?',
            copiedToTheClipboard: 'Copied to the clipboard!',
            close: 'Close',
            me: 'Me',
            customer: 'Customer',
            getDeliveryLink: 'Send Delivery Link',
            customerPhoneNumber: 'Customer phone number',
            pickupInstructionsOptional: 'Delivery note (Optional)',
            more: 'Delivery note'
        },
        deleteContactModal: {
            deleteThisContact: 'Delete this contact',
            yes: 'Yes',
            no: 'No',
        },
    },
    billing: {
        payments: {
            billing: 'Billing',
            payBalance: 'Pay balance',
            paymentMethods: 'Payment Methods',
        },
        payBalance: {
            unpaidDeliveries: 'Unpaid deliveries',
            allUnpaid: 'All unpaid',
            onlyUntil: 'Only until',
            deliveries: 'Deliveries',
            totalAmount: 'Total amount',
            cost: 'Transaction amount',
            transactionFee: 'Transaction fee',
            paymentMethod: 'Payment phone number',
            filterByBranch: 'Filter by branch',
            allBranches: 'All branches'
        },
        paymentMethods: {
            new: 'New',
            default: 'Default',
            remove: 'Remove',
        },
        newPaymentMethodModal: {
            addNewPaymentMethod: 'Add new payment method',
            phoneNumber: 'Phone number',
            momoCodeOptional: 'Momo code (optional)',
            add: 'Add',
        },
        removePaymentMethodModal: {
            removeThisPaymentMethod: 'Remove this payment method?',
            yes: 'Yes',
            no: 'No',
        },
        selectPaymentMethod: {
            newPaymentMethod: 'New payment phone number',
            paymentMethodNotFound: 'Payment phone number not found',
        },
        paymentDueAlert: {
            paymentRequired: 'Payment is due for completed deliveries.',
            pay: "Pay",
        },
    },
    alertAndValidationMessages: {
        pleaseCheckYourInternetConnection: 'Please check your internet connection.',
        somethingWentWrong: 'Something went wrong. Please contact support.',
        paymentReceivedSuccessful: 'Payment received successfully.',
        ourDriverWillBeThereSoon: 'Our driver will be there soon.',
        checkYourPhoneForPayment: 'Confirm payment on your phone.',
        orderReceivedSuccessful: 'Order received successfully. Our driver will be there soon.',
        verificationCodeRequired: 'Please enter the verification code.',
        verificationCodeMustBeNumber: 'Verification code must be a number.',
        verificationCodeValid: 'Verification code must have 6 digits.',
        termsOfUseRequired: 'You have to accept the Terms of Use',
        firstNameRequired: 'Please enter your first name.',
        lastNameRequired: 'Please enter your last name.',
        businessNameRequired: 'Please enter business name.',
        addressNameRequired: 'Please enter the address name',
        reasonCancelledDeliveryRequired: 'Please enter cancellation reason.',
        momoCodeMustBeNumber: 'MoMo code must be a number.',
        momoCodeMustHaveValidLength: 'MoMo code must be 5 or 6 numbers.',
        paymentMethodRequired: 'Please choose the payment method.',
        signInAndTryAgain: 'Please sign in and try again.',
        dontHaveCustomerAccount: "You don't have customer account. Please create one",
        phoneNumberRequired: "Please enter {{name}}'s phone number.",
        phoneNumberMustBeNumber: 'Phone number must be a number.',
        phoneNumberValid: 'Phone number is invalid.',
        houseNumberNotValid: 'House number is not valid',
        branchNameRequired: 'Please enter branch name',
        branchContactRequired: 'Please enter branch contact',
        branchAddressRequired: 'Please enter branch address',
        agentNameRequired: 'Please enter agent names',
        branchRequired: 'Please select branch',
        accountRequired: 'Please select account'
    },
    dashboard: {
        navbar: {
            signOut: 'Sign out',
            languages: 'Languages',
            turnDarkModeOn: 'Dark mode on',
            turnDarkModeOff: 'Turn dark mode off',
            thereIsNoNotifications: 'There is no notifications',
            new: 'New',
            viewed : 'Viewed',
            markAllAsRead: 'Mark all as read',
            notifications:'Notifications',
            unreadNotifications:'You have {{totalUnRead}} unread notifications'
        },
        sidebar: {
            customers: 'Customers',
            deliveries: 'Deliveries',
            newDelivery: 'New delivery',
            overview: 'Overview',
            billing: 'Billing',
            account: 'Account'
        },
    },
    application: {
        exception: {
            message: "Something unexpected happened and the issue was reported to our team.",
            callout: "Try refreshing the page by clicking the button below. If the issue persists, contact our customer support.",
            refresh: "Reload page"
        }
    },
    selectPaymentMethod: {
        searchPaymentMethods: 'Search payment phone numbers',
        notfound: 'Payment phone number not found',
        new: 'New',
    },
    account: {
        main: {
            account: 'Account',
            branches: 'Branches',
            agents: 'Agents',
        },
        branches: {
            branches: 'Branches',
            newBranch: 'New branch',
            delete: 'Delete',
            edit: 'Edit',
            branchesNotFound: 'There are no branches'
        },
        newBranchModal: {
            branchName: 'Branch name',
            addNewBranch: 'Add new branch',
            editBranch: 'Edit branch',
            add: 'Add',
            edit: 'Edit',
        },
        deleteBranchModal: {
            deleteThisBranch: 'Delete this branch',
            yes: 'Yes',
            no: 'No',
        },
        agents: {
            agents: 'Agents',
            newAgent: 'New agent',
            edit: 'Edit',
            delete: 'Delete',
            agentsNotFound: 'There are no agents',
            main: 'Main'
        },
        newAgentModal: {
            addNewAgent: 'Add new agent',
            fullName: 'Full name',
            phoneNumber: 'Phone number',
            editAgent: 'Edit agent',
            add: 'Add',
            edit: 'Edit',
            selectBranch: 'Select branch'
        },
        deleteAgentModal: {
            deleteThisAgent: 'Delete this agent',
            yes: 'Yes',
            no: 'No',
        },
    }
};
