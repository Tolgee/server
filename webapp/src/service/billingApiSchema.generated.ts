/**
 * This file was auto-generated by openapi-typescript.
 * Do not make direct changes to the file.
 */

export interface paths {
  "/v2/organizations/{organizationId}/billing/update-subscription": {
    put: operations["updateSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/self-hosted-ee/refresh-subscriptions": {
    put: operations["refreshSelfHostedEeSubscriptions"];
  };
  "/v2/organizations/{organizationId}/billing/refresh-subscription": {
    put: operations["refresh"];
  };
  "/v2/organizations/{organizationId}/billing/prepare-update-subscription": {
    put: operations["prepareUpdateSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/cancel-subscription": {
    put: operations["cancelSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/subscribe": {
    post: operations["subscribe"];
  };
  "/v2/organizations/{organizationId}/billing/self-hosted-ee/subscriptions": {
    get: operations["getSelfHostedEeSubscriptions"];
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
  "/v2/organizations/{organizationId}/billing/subscription": {
    get: operations["getSubscription"];
  };
  "/v2/organizations/{organizationId}/billing/self-hosted-ee/subscriptions/{subscriptionId}/expected-usage": {
    get: operations["getExpectedUsage"];
  };
  "/v2/organizations/{organizationId}/billing/self-hosted-ee/plans": {
    get: operations["getSelfHostedPlans"];
  };
  "/v2/organizations/{organizationId}/billing/plans": {
    get: operations["getCloudPlans"];
  };
  "/v2/organizations/{organizationId}/billing/invoices": {
    /** Returns organization invoices */
    get: operations["getInvoices"];
  };
  "/v2/organizations/{organizationId}/billing/invoices/{invoiceId}/usage": {
    get: operations["getUsage"];
  };
  "/v2/organizations/{organizationId}/billing/invoices/{invoiceId}/usage/{type}.csv": {
    get: operations["getUsageDetail"];
  };
  "/v2/organizations/{organizationId}/billing/invoices/{invoiceId}/pdf": {
    /** Returns organization invoices */
    get: operations["getInvoicePdf"];
  };
  "/v2/organizations/{organizationId}/billing/expected-usage": {
    get: operations["getExpectedUsage_1"];
  };
  "/v2/organizations/{organizationId}/billing/customer-portal": {
    get: operations["goToCustomerPortal"];
  };
  "/v2/organizations/{organizationId}/billing/billing-info": {
    get: operations["getBillingInfo"];
  };
  "/v2/organizations/{organizationId}/billing/self-hosted-ee/subscriptions/{subscriptionId}": {
    delete: operations["cancelEeSubscription"];
  };
}

export interface components {
  schemas: {
    UpdateSubscriptionRequest: {
      token: string;
    };
    CollectionModelSelfHostedEeSubscriptionModel: {
      _embedded?: {
        subscriptions?: components["schemas"]["SelfHostedEeSubscriptionModel"][];
      };
    };
    Links: { [key: string]: components["schemas"]["Link"] };
    PlanIncludedUsageModel: {
      seats: number;
      translationSlots: number;
      translations: number;
      mtCredits: number;
    };
    PlanPricesModel: {
      perSeat: number;
      perThousandTranslations: number;
      perThousandMtCredits: number;
      subscriptionMonthly: number;
      subscriptionYearly: number;
    };
    SelfHostedEePlanModel: {
      id: number;
      name: string;
      public: boolean;
      enabledFeatures: (
        | "GRANULAR_PERMISSIONS"
        | "PRIORITIZED_FEATURE_REQUESTS"
        | "PREMIUM_SUPPORT"
        | "DEDICATED_SLACK_CHANNEL"
        | "ASSISTED_UPDATES"
        | "DEPLOYMENT_ASSISTANCE"
        | "BACKUP_CONFIGURATION"
        | "TEAM_TRAINING"
        | "ACCOUNT_MANAGER"
      )[];
      prices: components["schemas"]["PlanPricesModel"];
      includedUsage: components["schemas"]["PlanIncludedUsageModel"];
    };
    SelfHostedEeSubscriptionModel: {
      id: number;
      currentPeriodStart?: number;
      currentPeriodEnd?: number;
      currentBillingPeriod: "MONTHLY" | "YEARLY";
      createdAt: number;
      plan: components["schemas"]["SelfHostedEePlanModel"];
      status:
        | "ACTIVE"
        | "CANCELED"
        | "PAST_DUE"
        | "UNPAID"
        | "ERROR"
        | "KEY_USED_BY_ANOTHER_INSTANCE";
      licenseKey?: string;
      estimatedCosts?: number;
    };
    CloudPlanModel: {
      id: number;
      name: string;
      free: boolean;
      enabledFeatures: (
        | "GRANULAR_PERMISSIONS"
        | "PRIORITIZED_FEATURE_REQUESTS"
        | "PREMIUM_SUPPORT"
        | "DEDICATED_SLACK_CHANNEL"
        | "ASSISTED_UPDATES"
        | "DEPLOYMENT_ASSISTANCE"
        | "BACKUP_CONFIGURATION"
        | "TEAM_TRAINING"
        | "ACCOUNT_MANAGER"
      )[];
      type: "PAY_AS_YOU_GO" | "FIXED" | "SLOTS_FIXED";
      prices: components["schemas"]["PlanPricesModel"];
      includedUsage: components["schemas"]["PlanIncludedUsageModel"];
    };
    CloudSubscriptionModel: {
      organizationId: number;
      plan: components["schemas"]["CloudPlanModel"];
      currentPeriodStart?: number;
      currentPeriodEnd?: number;
      currentBillingPeriod?: "MONTHLY" | "YEARLY";
      cancelAtPeriodEnd: boolean;
      estimatedCosts?: number;
      createdAt: number;
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
    CloudSubscribeRequest: {
      /** Id of the subscription plan */
      planId: number;
      period: "MONTHLY" | "YEARLY";
    };
    SubscribeModel: {
      url: string;
    };
    SelfHostedEeSubscribeRequest: {
      /** Id of the subscription plan */
      planId: number;
      period: "MONTHLY" | "YEARLY";
    };
    BuyMoreCreditsRequest: {
      priceId: number;
      amount: number;
    };
    BuyMoreCreditsModel: {
      url: string;
    };
    CollectionModelCloudPlanModel: {
      _embedded?: {
        plans?: components["schemas"]["CloudPlanModel"][];
      };
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
    AverageProportionalUsageItemModel: {
      total: number;
      unusedQuantity: number;
      usedQuantity: number;
      usedQuantityOverPlan: number;
    };
    SumUsageItemModel: {
      total: number;
      unusedQuantity: number;
      usedQuantity: number;
      usedQuantityOverPlan: number;
    };
    UsageModel: {
      subscriptionPrice?: number;
      seats: components["schemas"]["AverageProportionalUsageItemModel"];
      translations: components["schemas"]["AverageProportionalUsageItemModel"];
      credits?: components["schemas"]["SumUsageItemModel"];
      total: number;
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
      taxRatePercentage?: number;
      /** Whether pdf is ready to download. If not, wait around few minutes until it's generated. */
      pdfReady: boolean;
      hasUsage: boolean;
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
    Link: {
      href?: string;
      hreflang?: string;
      title?: string;
      type?: string;
      deprecation?: string;
      profile?: string;
      name?: string;
      templated?: boolean;
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
          "*/*": components["schemas"]["CloudSubscriptionModel"];
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
        "application/json": components["schemas"]["CloudSubscribeRequest"];
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
        "application/json": components["schemas"]["SelfHostedEeSubscribeRequest"];
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
          "*/*": components["schemas"]["CollectionModelCloudPlanModel"];
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
  getSubscription: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["CloudSubscriptionModel"];
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
  getExpectedUsage: {
    parameters: {
      path: {
        organizationId: number;
        subscriptionId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["UsageModel"];
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
          "*/*": components["schemas"]["CollectionModelCloudPlanModel"];
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
  getUsage: {
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
          "*/*": components["schemas"]["UsageModel"];
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
  getUsageDetail: {
    parameters: {
      path: {
        organizationId: number;
        invoiceId: number;
        type: "SEATS" | "TRANSLATIONS";
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "text/csv": string;
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
  getExpectedUsage_1: {
    parameters: {
      path: {
        organizationId: number;
      };
    };
    responses: {
      /** OK */
      200: {
        content: {
          "*/*": components["schemas"]["UsageModel"];
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
}

export interface external {}
