import test from 'node:test'
import assert from 'node:assert/strict'
import { exercisesChanged } from './check-revisions.mjs'

const catalog = (exercise, text = { name: 'Squat' }) => ({
  exercises: [exercise],
  text: { en: { squat: text }, fr: { squat: { name: 'Squat' } } },
})
const squat = { slug: 'squat', measurement: 'REPS' }

test('an unchanged exercise needs no bump', () => {
  assert.deepEqual(exercisesChanged(catalog(squat), catalog(squat)), [])
})

test('a changed field without a higher revision fails', () => {
  const failures = exercisesChanged(catalog(squat), catalog({ ...squat, measurement: 'TIME' }))
  assert.deepEqual(failures, [{ slug: 'squat', revision: 1, needed: 2 }])
})

test('changed text without a higher revision fails', () => {
  const failures = exercisesChanged(catalog(squat), catalog(squat, { name: 'Air squat' }))
  assert.equal(failures.length, 1)
})

test('a change with a higher revision passes', () => {
  const head = catalog({ ...squat, measurement: 'TIME', revision: 2 })
  assert.deepEqual(exercisesChanged(catalog(squat), head), [])
})

test('the next revision is above the current one', () => {
  const before = catalog({ ...squat, revision: 3 })
  const failures = exercisesChanged(before, catalog({ ...squat, measurement: 'TIME', revision: 3 }))
  assert.deepEqual(failures, [{ slug: 'squat', revision: 3, needed: 4 }])
})

test('a new exercise needs no bump', () => {
  const base = catalog(squat)
  const head = { exercises: [squat, { slug: 'lunge' }], text: base.text }
  assert.deepEqual(exercisesChanged(base, head), [])
})

test('key order does not count as a change', () => {
  const reordered = { measurement: 'REPS', slug: 'squat' }
  assert.deepEqual(exercisesChanged(catalog(squat), catalog(reordered)), [])
})
