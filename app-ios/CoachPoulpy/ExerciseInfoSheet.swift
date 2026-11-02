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
    // From the picker the sheet can move along the exercise's variations; [current] is the one shown and added.
    @State private var current: String?
    @State private var listing = false
    @State private var tab = ExerciseTab.muscles

    var body: some View {
        if let detail = graph.exerciseList(languageCode: Language.code).detail(slug: current ?? slug) {
            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        if adding && listing {
                            variationsList(detail)
                        } else if adding {
                            variationsPage(detail)
                        } else {
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
                        WorkedMuscles(graph: graph, detail: detail)
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
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                    .padding(.top, 20)
                    .padding(.bottom, 16)
                }
                if !(adding && listing) {
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
            }
            .background(PoulpyColor.surface)
            .presentationDetents([.large])
        }
    }

    /// The picker's layout, like an exercise's page: the variations button and close, the name and equipment, then the tabs.
    @ViewBuilder
    private func variationsPage(_ detail: ExerciseDetail) -> some View {
        HStack {
            if !detail.variations.isEmpty {
                Button { listing = true } label: { VariationButtonLabel(detail: detail) }
                    .buttonStyle(.plain)
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel(Text("exercise.variationsA11y \(detail.variationPosition)"))
                    .accessibilityAddTraits(.isButton)
            }
            Spacer(minLength: 0)
            closeButton
        }
        ExerciseHeading(detail: detail)
        Picker("exercise.muscles", selection: $tab) {
            Text("exercise.muscles").tag(ExerciseTab.muscles)
            Text("exercise.how").tag(ExerciseTab.how)
        }
        .pickerStyle(.segmented)
        ExerciseTabContent(graph: graph, detail: detail, tab: tab)
    }

    /// The group, easiest first; picking one shows its details here.
    @ViewBuilder
    private func variationsList(_ detail: ExerciseDetail) -> some View {
        HStack(alignment: .center) {
            Button { listing = false } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(PoulpyColor.onSurface)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel(Text("action.back"))
            VStack(alignment: .leading, spacing: 2) {
                Text("exercise.variations").font(PoulpyFont.headline).accessibilityAddTraits(.isHeader)
                Text("exercise.variationsHint").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            closeButton
        }
        VariationRungs(variations: detail.variations, pick: { picked in
            current = picked
            tab = .muscles
            listing = false
        }, onCurrent: { listing = false })
    }

    private var closeButton: some View {
        Button { dismiss() } label: {
            Image(systemName: "xmark")
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(PoulpyColor.onSurface)
                .frame(width: 44, height: 44)
        }
        .accessibilityLabel("action.close")
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

/// Front and back figures, on the person's own figure, with the muscles the exercise works lit.
struct WorkedMuscles: View {
    let graph: AppGraph
    let detail: ExerciseDetail

    var body: some View {
        let figure = (graph.profile(memory: UserDefaultsStore()).ui.value as! ProfileUi).bodyFigure ?? .shoulders
        // The muscles are named in text beside it, so the picture is hidden from VoiceOver.
        HStack(alignment: .top, spacing: 0) {
            side(figure, .front, "profile.figureFront")
            side(figure, .back, "profile.figureBack")
        }
        .accessibilityHidden(true)
    }

    private func side(_ figure: BodyFigure, _ view: Shared.BodyView, _ caption: LocalizedStringKey) -> some View {
        VStack(spacing: 4) {
            BodyMapFigure(figure: figure, view: view, selected: detail.workedAreas, width: 136)
            Text(caption).font(PoulpyFont.label).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity)
    }
}
