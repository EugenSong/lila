package views.html
package oAuth

import lila.api.WebContext
import lila.app.templating.Environment.{ given, * }
import lila.app.ui.ScalatagsTemplate.{ *, given }
import lila.user.User
import lila.oauth.AuthorizationRequest
import lila.oauth.OAuthScope
import controllers.routes
import lila.common.LightUser

object authorize:

  val ringsImage = img(
    cls := "oauth__logo",
    alt := "linked rings icon",
    src := assetUrl("images/icons/linked-rings.png")
  )

  def apply(prompt: AuthorizationRequest.Prompt, me: User, authorizeUrl: String)(using WebContext) =
    val isDanger           = prompt.scopes.intersects(OAuthScope.dangerList)
    val buttonClass        = s"button${isDanger so " button-red confirm text"}"
    val buttonDelay        = if (isDanger) 5000 else 2000
    val otherUserRequested = prompt.userId.filterNot(me.is(_)).map(lightUserFallback)
    views.html.base.layout(
      title = "Authorization",
      moreCss = cssTag("oauth"),
      moreJs = embedJsUnsafe(
        // ensure maximum browser compatibility
        s"""setTimeout(function(){var el=document.getElementById('oauth-authorize');el.removeAttribute('disabled');el.setAttribute('class','$buttonClass')}, $buttonDelay);"""
      ),
      csp = defaultCsp.withLegacyCompatibility.some
    ) {
      main(cls := "oauth box box-pad force-ltr")(
        div(cls := "oauth__top")(
          ringsImage,
          h1("Authorize"),
          strong(code(prompt.redirectUri.clientOrigin))
        ),
        prompt.redirectUri.insecure option flashMessage("warning")("Does not use a secure connection"),
        postForm(action := authorizeUrl)(
          p(
            "Grant access to your ",
            strong(otherUserRequested.fold(me.username)(_.name)),
            " account:"
          ),
          if (prompt.scopes.isEmpty) ul(li("Only public data"))
          else
            ul(cls := "oauth__scopes")(
              prompt.scopes.value.map: scope =>
                li(cls := List("danger" -> OAuthScope.dangerList.has(scope)))(scope.name())
            ),
          form3.actions(
            a(href := prompt.cancelUrl)("Cancel"),
            otherUserRequested match
              case Some(otherUser) =>
                a(cls := "button", href := switchLoginUrl(otherUser.name.some))("Log in as ", otherUser.name)
              case None =>
                submitButton(
                  cls      := s"$buttonClass disabled",
                  dataIcon := isDanger.option(licon.CautionTriangle),
                  disabled := true,
                  id       := "oauth-authorize",
                  title := s"The website ${prompt.redirectUri.host | prompt.redirectUri.withoutQuery} will get access to your Lichess account. Continue?"
                )("Authorize")
          ),
          footer(prompt.redirectUri.withoutQuery, isDanger, otherUserRequested)
        )
      )
    }

  private def switchLoginUrl(to: Option[UserName])(using ctx: WebContext) =
    addQueryParams(routes.Auth.login.url, Map("switch" -> to.fold("1")(_.value), "referrer" -> ctx.req.uri))

  private def footer(redirectUrl: String, isDanger: Boolean, otherUserRequested: Option[LightUser])(using
      ctx: WebContext
  ) =
    div(cls := "oauth__footer")(
      ctx.me ifTrue otherUserRequested.isEmpty map { me =>
        p(
          "Not ",
          me.username,
          "? ",
          a(href := switchLoginUrl(none))(trans.signIn())
        )
      },
      p(cls := List("danger" -> isDanger))("Not owned or operated by lichess.org"),
      p(cls := "oauth__redirect")(
        "Will redirect to ",
        redirectUrl
      )
    )
