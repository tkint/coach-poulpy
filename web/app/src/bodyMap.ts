// The areas of CpBodyMap an exercise or a workout lights, and the muscle group each area stands for.
import type { BodyAreas } from '@coach-poulpy/ui'

/** The heat the API sent for each area (1 to 3), as areas lit on both sides. */
export function heatAreas(heat: Record<string, number>): BodyAreas {
  const areas: BodyAreas = {}
  for (const [area, level] of Object.entries(heat)) if (level === 1 || level === 2 || level === 3) areas[area] = { side: 'both', heat: level }
  return areas
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

/** The muscle group an area of the figure stands for, or nothing for an area that stands for none. */
export const groupOfArea = (area: string): string | undefined => GROUP_OF[area]

/** The areas lit by the muscle groups picked (full body lights every muscle area), on both sides. */
export function groupAreas(groups: string[]): BodyAreas {
  const fullBody = groups.includes('full_body')
  const areas: BodyAreas = {}
  for (const area of new Set([...Object.keys(GROUP_OF), ...MUSCLE_AREAS])) {
    const group = GROUP_OF[area]
    if ((group && groups.includes(group)) || (fullBody && MUSCLE_AREAS.has(area))) areas[area] = 'both'
  }
  return areas
}
