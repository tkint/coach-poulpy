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
