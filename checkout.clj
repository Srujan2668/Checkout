(config
    (text-field
        :name         "clientId"
        :label        "Enter clientId"
        :placeholder  ""
        :required     true
        (api-config-field)
    )

    (password-field
        :name         "clientSecret"
        :label        "Enter clientSecret"
        :placeholder  "Enter Api Key"
        :required     true
        (api-config-field :masked true)
    )
    (text-field
        :name "subDomain"
        :label "Domain name"
        :placeholder "Sub-domain name"
    )   


   

    (oauth2/authorization-code-flow-client-credentials
        (token
            (source
                (http/post
                    :url "https://access.checkout.com/connect/token"
                    (body-params
                        "response_type" "code"
                        "client_id" "{clientId}"
                        "client_secret" "{clientSecret}"
                        "scope" " Access_to_all_Disputes_resources View_disputes"
                        "Environment" "Sandbox"
                    )
                )
            )

            (fields
                access-token :<= "access_token"
                token-type :<= "token_type"
                refresh_token 
                scope :<= "scope"
                realm-id :<= "realmId"
                expires-in :<= "expires_in"
            )
        )
    )

)

    (default-source (http/get :base-url "https://api.{subDomain}.checkout.com/disputes"
        (header-params "Accept" "application/json"))
            (paging/url-key :offset-query-param-initial-value
                            :offset-query-param-name
                            :limit 250
                            :limit-query-param-name "limit"
            )
            (auth/oauth2/authorization-code-flow-client-credentials)
        (error-handler
            (when :status 401 :message "Unauthorized" :action refresh)
            (when :status 429 :message "Unprocessable paging")
        )
            
)

(entity disputes
        (api-docs-url "https://api-reference.checkout.com/#operation/getDisputes")
        (source (http/get : url "/disputes"))

        (sync-plan
          (change-capture-cursor
            (query-params "sort" "desc")
            (extract-path "data")
           (subset/by-time (query-params "from" "$FROM"
                                         "to" "$TO")
                           (format "yyyy-MM-dd'T'HH:mm:ssZ")
                           (step-size "24 hr")
                           (initial  "2023-01-01T00:00:00Z")
                        ;;    (save)
                        )))


)

