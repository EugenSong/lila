package views.html
package practice

import lila.api.WebContext
import lila.app.templating.Environment.{ given, * }
import lila.app.ui.ScalatagsTemplate.{ *, given }

import controllers.routes

object index:

  def apply(data: lila.practice.UserPractice)(using ctx: WebContext) =
    views.html.base.layout(
      title = "Practice chess positions",
      moreCss = cssTag("practice.index"),
      moreJs = embedJsUnsafeLoadThen(s"""$$('.do-reset').on('click', function() {
if (confirm('You will lose your practice progress!')) this.parentNode.submit();
});"""),
      openGraph = lila.app.ui
        .OpenGraph(
          title = "Practice your chess",
          description = "Learn how to master the most common chess positions",
          url = s"$netBaseUrl${routes.Practice.index}"
        )
        .some
    ) {
      main(cls := "page-menu force-ltr")(
        st.aside(cls := "page-menu__menu practice-side")(
          i(cls := "fat"),
          h1("Practice"),
          h2("makes your chess perfect"),
          div(cls := "progress")(
            div(cls := "text")("Progress: ", data.progressPercent, "%"),
            div(cls := "bar", style := s"width: ${data.progressPercent}%")
          ),
          postForm(action := routes.Practice.reset)(
            if (ctx.isAuth) (data.nbDoneChapters > 0) option a(cls := "do-reset")("Reset my progress")
            else a(href := routes.Auth.signup)("Sign up to save your progress")
          )
        ),
        div(cls := "page-menu__content practice-app")(
          data.structure.sections.filter(s => !s.hide || isGranted(_.PracticeConfig)) map { section =>
            st.section(
              h2(section.name),
              div(cls := "studies")(
                section.studies.filter(s => !s.hide || isGranted(_.PracticeConfig)).map { stud =>
                  val prog = data.progressOn(stud.id)
                  a(
                    cls  := s"study ${if (prog.complete) "done" else "ongoing"}",
                    href := routes.Practice.show(section.id, stud.slug, stud.id)
                  )(
                    ctx.isAuth option span(cls := "ribbon-wrapper")(
                      span(cls := "ribbon")(prog.done, " / ", prog.total)
                    ),
                    i(cls := s"${stud.id}"),
                    span(cls := "text")(
                      h3(stud.name),
                      em(stud.desc)
                    )
                  )
                }
              )
            )
          }
        )
      )
    }
