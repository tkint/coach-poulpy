// The body map figures for CpBodyMap: the shapes are the apps' (design/body-map.json), lit with the heat the API sent for each area.
import shapes from '../../../design/body-map.json'
import type { BodyMapFigure } from '@coach-poulpy/ui'

type Region = { key: string | null; d: string; skin: boolean }
const figure = shapes.shoulders as Record<'front' | 'back', Region[]>

/** The front and back figure, each area lit at its heat (1 to 3) and the head outlined. */
export function bodyMapFigures(heat: Record<string, number>, captions: { front: string; back: string }): BodyMapFigure[] {
  const view = (regions: Region[], caption: string): BodyMapFigure => ({
    caption,
    paths: regions.map((region) => {
      const level = region.key ? heat[region.key] : undefined
      return region.skin ? { d: region.d, outline: true } : { d: region.d, heat: level === 1 || level === 2 || level === 3 ? level : undefined }
    }),
  })
  return [view(figure.front, captions.front), view(figure.back, captions.back)]
}

/** The muscle group (as the catalog names it, in lowercase) each area of the figure belongs to, as shared/BodyMap.kt reads them; the joints, the head and the spine belong to none. */
const GROUP_OF: Record<string, string> = {
  pectoral: 'chest',
  back: 'back',
  scapula: 'back',
  'lower-back': 'back',
  deltoid: 'shoulders',
  biceps: 'biceps',
  triceps: 'triceps',
  forearm: 'forearms',
  abs: 'core',
  obliques: 'core',
  glutes: 'glutes',
  quads: 'quadriceps',
  hamstrings: 'hamstrings',
  calves: 'calves',
  hip: 'hips',
  adductors: 'hips',
  neck: 'neck',
  trapezius: 'neck',
}
/** What "full body" lights: every muscle area, but not the joints, the spine or the head. */
const MUSCLE_AREAS = new Set(['trapezius', 'deltoid', 'pectoral', 'serratus', 'biceps', 'triceps', 'forearm', 'abs', 'obliques', 'back', 'scapula', 'lower-back', 'glutes', 'quads', 'adductors', 'hamstrings', 'calves'])

const baseOf = (key: string) => key.replace(/-(left|right)$/, '')
/** The muscle group an area of the figure stands for, or nothing for an area that stands for none. */
export const groupOfArea = (key: string): string | undefined => GROUP_OF[baseOf(key)]

/** The figures as a picker: an area is lit when its muscle group is picked (or the exercise is full body), and tappable when it has a group, named by [label]. */
export function musclePickerFigures(groups: string[], captions: { front: string; back: string }, label: (group: string) => string): BodyMapFigure[] {
  const fullBody = groups.includes('full_body')
  const view = (regions: Region[], caption: string): BodyMapFigure => ({
    caption,
    paths: regions.map((region) => {
      if (region.skin) return { d: region.d, outline: true }
      const group = region.key ? groupOfArea(region.key) : undefined
      const lit = region.key ? (group ? groups.includes(group) : false) || (fullBody && MUSCLE_AREAS.has(baseOf(region.key))) : false
      return { d: region.d, key: region.key ?? undefined, label: group ? label(group) : undefined, heat: lit ? 3 : undefined }
    }),
  })
  return [view(figure.front, captions.front), view(figure.back, captions.back)]
}
