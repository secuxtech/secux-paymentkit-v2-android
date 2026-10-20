# Payment credentials

This cleanup removes the identified embedded administrator, test-account and
wallet credentials from these inactive projects. Keep credentials outside version
control. Removing a value from source does not revoke it or erase Git history.

## Current behavior

- Removed dormant administrator-password fallbacks and sample device IV payloads from SDK comments.
- Removed embedded accounts, the merchant test password and automatic network actions from the example app. `configureDemo(account, adminName, adminPassword)` accepts caller-supplied disposable test credentials; call a demo action explicitly only after it returns true.
- Removed the example's signing passwords and developer keystore path. Release APKs are unsigned until signing is configured outside version control.
- Removed HTTP request-body/response logging and refund/refill payload logging, which could expose passwords, tokens or device encryption material.
- README snippets use caller-provided `userPassword` / `newPassword` variables instead of shared passwords.

## Re-enabling development

Obtain fresh, narrowly scoped test credentials and provide them at runtime through
the existing SDK configuration APIs (or the example's explicit configuration
method, where provided). Do not restore the removed values from Git history or
commit replacement secrets in source, examples, resources or signing settings.
Production administrator access belongs behind a trusted service; packaging an
administrator password in a mobile application does not keep it secret.

The published historical credentials must be treated as exposed. Their owner
must revoke or rotate any values still accepted by the server before reuse of
these projects. This source cleanup does not change server accounts, remove old
tags/releases, rewrite history or automatically close secret-scanning alerts.
