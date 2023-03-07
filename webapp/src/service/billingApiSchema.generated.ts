/**
 * This file was auto-generated by openapi-typescript.
 * Do not make direct changes to the file.
 */

export interface paths {
  "/v2/organizations/{organizationId}/billing/update-subscription": {
    put: operations["updateSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/refresh-subscription": {
    put: operations["refresh"];
  };
  "/v2/organizations/{organizationId}/billing/refresh-self-hosted-ee-subscriptions": {
    put: operations["refreshSelfHostedEeSubscriptions"];
  };
  "/v2/organizations/{organizationId}/billing/prepare-update-subscription": {
    put: operations["prepareUpdateSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/cancel-subscription": {
    put: operations["cancelSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/cancel-self-hosted-ee-subscription/{subscriptionId}": {
    put: operations["cancelEeSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/subscribe": {
    post: operations["subscribe"];
  };
  "/v2/organizations/{organizationId}/billing/setup-ee": {
    post: operations["setupEeSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/buy-more-credits": {
    post: operations["getBuyMoreCreditsCheckoutSessionUrl"];
  };
  "/v2/public/billing/plans": {
    get: operations["getPlans"];
  };
  "/v2/public/billing/mt-credit-prices": {
    get: operations["getMtCreditPrices"];
  };
  "/v2/organizations/{organizationId}/billing/self-hosted-ee-subscriptions": {
    get: operations["getSelfHostedEeSubscriptions"];
  };
  "/v2/organizations/{organizationId}/billing/self-hosted-ee-plans": {
    get: operations["getSelfHostedPlans"];
  };
  "/v2/organizations/{organizationId}/billing/plans": {
    get: operations["getCloudPlans"];
  };
  "/v2/organizations/{organizationId}/billing/invoices/{invoiceId}/pdf": {
    /** Returns organization invoices */
    get: operations["getInvoicePdf"];
  };
  "/v2/organizations/{organizationId}/billing/invoices/": {
    /** Returns organization invoices */
    get: operations["getInvoices"];
  };
  "/v2/organizations/{organizationId}/billing/customer-portal": {
    get: operations["goToCustomerPortal"];
  };
  "/v2/organizations/{organizationId}/billing/billing-info": {
    get: operations["getBillingInfo"];
  };
  "/v2/organizations/{organizationId}/billing/active-plan": {
    get: operations["getActivePlan"];
  };
}

export interface components {
  schemas: {
    UpdateSubscriptionRequest: {
      token: string;
    };
    ActivePlanModel: {
      id: number;
      name: string;
      translationLimit?: number;
      includedMtCredits?: number;
      monthlyPrice: number;
      yearlyPrice: number;
      enabledFeatures: "GRANULAR_PERMISSIONS"[];
      currentPeriodEnd?: number;
      cancelAtPeriodEnd: boolean;
      currentBillingPeriod?: "MONTHLY" | "YEARLY";
      free: boolean;
    };
    CollectionModelSelfHostedEeSubscriptionModel: {
      _embedded?: {
        subscriptions?: components["schemas"]["SelfHostedEeSubscriptionModel"][];
      };
    };
    SelfHostedEePlanModel: {
      id: number;
      name: string;
      public: boolean;
      enabledFeatures: "GRANULAR_PERMISSIONS"[];
      includedSeats: number;
      pricePerSeat: number;
    };
    SelfHostedEeSubscriptionModel: {
      id: number;
      currentPeriodEnd?: number;
      cancelAtPeriodEnd: boolean;
      createdAt: string;
      plan: components["schemas"]["SelfHostedEePlanModel"];
      status: "ACTIVE" | "CANCELLED" | "PAST_DUE" | "UNPAID" | "ERROR";
    };
    UpdateSubscriptionPrepareRequest: {
      /** Id of the subscription plan */
      planId: number;
      period: "MONTHLY" | "YEARLY";
    };
    SubscriptionUpdatePreviewItem: {
      description: string;
      amount: number;
      taxRate: number;
    };
    SubscriptionUpdatePreviewModel: {
      items: components["schemas"]["SubscriptionUpdatePreviewItem"][];
      total: number;
      amountDue: number;
      updateToken: string;
      prorationDate: number;
      endingBalance: number;
    };
    SubscribeRequest: {
      /** Id of the subscription plan */
      planId: number;
      period: "MONTHLY" | "YEARLY";
    };
    SubscribeModel: {
      url: string;
    };
    SetupEeRequest: {
      /** Id of the subscription plan */
      planId: number;
    };
    BuyMoreCreditsRequest: {
      priceId: number;
      amount: number;
    };
    BuyMoreCreditsModel: {
      url: string;
    };
    CollectionModelPlanModel: {
      _embedded?: {
        plans?: components["schemas"]["PlanModel"][];
      };
    };
    PlanModel: {
      id: number;
      name: string;
      translationLimit?: number;
      includedMtCredits?: number;
      monthlyPrice: number;
      yearlyPrice: number;
      free: boolean;
      enabledFeatures: "GRANULAR_PERMISSIONS"[];
    };
    CollectionModelMtCreditsPriceModel: {
      _embedded?: {
        prices?: components["schemas"]["MtCreditsPriceModel"][];
      };
    };
    MtCreditsPriceModel: {
      id: number;
      price: number;
      amount: number;
    };
    CollectionModelSelfHostedEePlanModel: {
      _embedded?: {
        plans?: components["schemas"]["SelfHostedEePlanModel"][];
      };
    };
    InvoiceModel: {
      id: number;
      /** The number on the invoice */
      number: string;
      createdAt: number;
      /** The Total amount with tax */
      total: number;
      /** Whether pdf is ready to download. If not, wait around few minutes until it's generated. */
      pdfReady: boolean;
    };
    PageMetadata: {
      size?: number;
      totalElements?: number;
      totalPages?: number;
      number?: number;
    };
    PagedModelInvoiceModel: {
      _embedded?: {
        invoices?: components["schemas"]["InvoiceModel"][];
      };
      page?: components["schemas"]["PageMetadata"];
    };
    GoToCustomerPortalModel: {
      url: string;
    };
    BillingInfoModel: {
      name?: string;
      street?: string;
      street2?: string;
      city?: string;
      zip?: string;
      state?: string;
      countryIso?: string;
      registrationNo?: string;
      vatNo?: string;
      email?: string;
    };
  };
}

export interface operations {
  updateSubscription: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: unknown;
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["UpdateSubscriptionRequest"];
      };
    };
  };
  refresh: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["ActivePlanModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  refreshSelfHostedEeSubscriptions: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["CollectionModelSelfHostedEeSubscriptionModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  prepareUpdateSubscription: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["SubscriptionUpdatePreviewModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["UpdateSubscriptionPrepareRequest"];
      };
    };
  };
  cancelSubscription: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: unknown;
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  cancelEeSubscription: {
    parameters: {
      path: {
        organizationId: number;
        subscriptionId: number;
      };
    };
    responses: {
      /** OK */
      200: unknown;
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  subscribe: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["SubscribeModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["SubscribeRequest"];
      };
    };
  };
  setupEeSubscription: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["SubscribeModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["SetupEeRequest"];
      };
    };
  };
  getBuyMoreCreditsCheckoutSessionUrl: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["BuyMoreCreditsModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["BuyMoreCreditsRequest"];
      };
    };
  };
  getPlans: {
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["CollectionModelPlanModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  getMtCreditPrices: {
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["CollectionModelMtCreditsPriceModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  getSelfHostedEeSubscriptions: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["CollectionModelSelfHostedEeSubscriptionModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  getSelfHostedPlans: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["CollectionModelSelfHostedEePlanModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  getCloudPlans: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["CollectionModelPlanModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  /** Returns organization invoices */
  getInvoicePdf: {
    parameters: {
      path: {
        organizationId: number;
        invoiceId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "application/pdf": string;
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  /** Returns organization invoices */
  getInvoices: {
    parameters: {
      path: {
        organizationId: number;
      };
      query: {
        /** Zero-based page index (0..N) */
        page?: number;
        /** The size of the page to be returned */
        size?: number;
        /** Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported. */
        sort?: string[];
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["PagedModelInvoiceModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  goToCustomerPortal: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["GoToCustomerPortalModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  getBillingInfo: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["BillingInfoModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
  getActivePlan: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["ActivePlanModel"];
        };
      };
      /** Bad Request */
      400: {
        content: {
          "*/*": string;
        };
      };
      /** Not Found */
      404: {
        content: {
          "*/*": string;
        };
      };
    };
  };
}

export interface external {}
