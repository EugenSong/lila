package views.html
package base

import lila.api.WebContext
import lila.app.templating.Environment.{ given, * }
import lila.app.ui.ScalatagsTemplate.{ *, given }

import controllers.routes

object notFound:

  def apply()(using WebContext) =
    layout(
      title = "Page not found",
      moreJs = prismicJs,
      moreCss = cssTag("not-found"),
      csp = isGranted(_.Prismic) option defaultCsp.withPrismic(true)
    ) {
      main(cls := "not-found page-small box box-pad")(
        header(
          h1("404"),
          div(
            strong("Page not found!"),
            p(
              "Return to ",
              a(href := routes.Lobby.home)("the homepage"),
              span(cls := "or-play")(" or play this mini-game")
            )
          )
        ),
        div(cls := "game")(
          iframe(
            src            := assetUrl(s"vendor/ChessPursuit/bin-release/index.html"),
            st.frameborder := 0,
            widthA         := 400,
            heightA        := 500,
            frame.credentialless
          ),
          p(cls := "credits")(
            a(href := "https://github.com/Saturnyn/ChessPursuit")("ChessPursuit"),
            " courtesy of ",
            a(href := "https://github.com/Saturnyn")("Saturnyn")
          )
        )
      )
    }
