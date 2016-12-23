// Rollup plugins.
import buble from 'rollup-plugin-buble'
import cjs from 'rollup-plugin-commonjs'
import replace from 'rollup-plugin-replace'
import resolve from 'rollup-plugin-node-resolve'

export default {
  dest: 'server.js',
  entry: 'src/server.js',
  format: 'iife',
  plugins: [
    buble({
      // enable object spread operator
      objectAssign: 'Object.assign'
    }),
    cjs({
      namedExports: {
        'node_modules/react-dom/server.js': ['renderToString']
      }
    }),
    replace({'process.env.NODE_ENV': JSON.stringify('development')}),
    resolve({
      main: true
    })
  ],
  sourceMap: false
}
