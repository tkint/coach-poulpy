import SwiftUI
import Shared

/// What the builder shows of an exercise: its muscles and equipment, the body map with the muscles it works lit on the
/// person's figure, the steps, and one button. From the picker it is "Add to the workout" ([adding]), which goes on to
/// the add sheet; from a step's sheet it is "Back to the step"; from the workout page it is "Close" ([closeLabel]).
struct ExerciseInfoSheet: View {
    let graph: AppGraph
    let slug: String
    var adding = true
    var closeLabel: LocalizedStringKey = "exercise.backToStep"
    let onAction: (String) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        if let detail = graph.exerciseList(languageCode: Language.code).detail(slug: slug) {
            let figure = (graph.profile(memory: UserDefaultsStore()).ui.value as! ProfileUi).bodyFigure ?? .shoulders
            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        HStack(alignment: .top) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(detail.eyebrow.uppercased())
                                    .font(PoulpyFont.label)
                                    .tracking(1)
                                    .foregroundStyle(PoulpyColor.primary)
                                Text(detail.name)
                                    .font(PoulpyFont.title)
                                    .foregroundStyle(PoulpyColor.onSurface)
                                    .accessibilityAddTraits(.isHeader)
                            }
                            Spacer(minLength: 0)
                            Button { dismiss() } label: {
                                Image(systemName: "xmark")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundStyle(PoulpyColor.onSurface)
                                    .frame(width: 44, height: 44)
                            }
                            .accessibilityLabel("action.close")
                        }
                        HStack(alignment: .top, spacing: 12) {
                            factCard("exercise.muscles", detail.muscles)
                            factCard("exercise.equipment", detail.equipment)
                        }
                        // The text above already names the muscles, so the picture is hidden from VoiceOver.
                        HStack(alignment: .top, spacing: 0) {
                            side(figure, .front, "profile.figureFront", detail.workedAreas)
                            side(figure, .back, "profile.figureBack", detail.workedAreas)
                        }
                        .accessibilityHidden(true)
                        VStack(alignment: .leading, spacing: 10) {
                            Text("exercise.how").font(PoulpyFont.headline).accessibilityAddTraits(.isHeader)
                            ForEach(Array(detail.instructions.enumerated()), id: \.offset) { index, step in
                                HStack(alignment: .top, spacing: 12) {
                                    Text(verbatim: "\(index + 1)")
                                        .font(.poulpy(.subheadline).weight(.heavy))
                                        .foregroundStyle(PoulpyColor.onPrimary)
                                        .frame(width: 28, height: 28)
                                        .background(PoulpyColor.primary, in: Circle())
                                        .accessibilityHidden(true)
                                    Text(step).font(PoulpyFont.body)
                                }
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                    .padding(.top, 20)
                    .padding(.bottom, 16)
                }
                Divider()
                Button { onAction(detail.slug) } label: {
                    Group {
                        if adding {
                            Label("exercise.addHere", systemImage: "plus")
                        } else {
                            Text(closeLabel)
                        }
                    }
                        .font(.poulpy(.callout).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onPrimary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(PoulpyColor.primary, in: Capsule())
                }
                .buttonStyle(.plain)
                .padding(.horizontal, 20)
                .padding(.top, 12)
                .padding(.bottom, 8)
            }
            .background(PoulpyColor.surface)
            .presentationDetents([.large])
        }
    }

    private func side(_ figure: BodyFigure, _ view: Shared.BodyView, _ caption: LocalizedStringKey, _ worked: Set<String>) -> some View {
        VStack(spacing: 4) {
            BodyMapFigure(figure: figure, view: view, selected: worked)
            Text(caption).font(PoulpyFont.label).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity)
    }

    private func factCard(_ title: LocalizedStringKey, _ body: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .textCase(.uppercase)
                .font(PoulpyFont.label)
                .tracking(1)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            Text(body).font(.poulpy(.callout).weight(.bold)).foregroundStyle(PoulpyColor.onSurface)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: 16))
    }
}
