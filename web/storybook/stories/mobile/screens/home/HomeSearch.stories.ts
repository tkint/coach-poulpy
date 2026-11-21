import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHomeSearch from './home-search.html?raw'
import screenHomeSearchEmpty from './home-search-empty.html?raw'
import screenHomeSearchDuration from './home-search-duration.html?raw'
import screenHomeSearchZones from './home-search-zones.html?raw'

// Search and filters over the diary's past sessions (#511). The search button on the Past heading, beside the calendar button, opens them;
// the filters are the library's (Equipment, Focus, Zones) plus Duration and Distance.
const meta = {
  title: 'Mobile/Screens/Home/Search',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: { issue: 511 }, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Search · "squat" with an equipment and a zones filter on: the matching sessions, by day, with a way to clear the filters. */
export const HomeSearch: Story = { name: 'Search and filters', ...screen(screenHomeSearch) }
/** Search · nothing matches: Poulpy says so and offers to clear the search and the filters. */
export const HomeSearchEmpty: Story = { name: 'No results', ...screen(screenHomeSearchEmpty) }
/** Search · the Duration filter, ranges to tick. Distance is the same sheet, in kilometres. */
export const HomeSearchDuration: Story = { name: 'Filter by duration', ...screen(screenHomeSearchDuration) }
/** Search · the Zones filter, the library's body map. Equipment and Focus open the same sheets as the library's. */
export const HomeSearchZones: Story = { name: 'Filter by zones', ...screen(screenHomeSearchZones) }
