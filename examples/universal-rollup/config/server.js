// Rollup plugins.
import babel from 'rollup-plugin-babel'
import cjs from 'rollup-plugin-commonjs'
import globals from 'rollup-plugin-node-globals'
import replace from 'rollup-plugin-replace'
import resolve from 'rollup-plugin-node-resolve'

export default {
  dest: 'server.js',
  entry: 'src/server.js',
  format: 'iife',
  plugins: [
    babel({
      exclude: 'node_modules/**',
      presets: [['es2015', {'modules': false}], 'react'],
      'plugins': [
        'external-helpers'
      ]
    }),
    cjs({
      exclude: 'node_modules/process-es6/**',
      include: [
        'node_modules/fbjs/**',
        'node_modules/object-assign/**',
        'node_modules/react/**',
        'node_modules/react-dom/**',
        'node_modules/react-router/**'
      ],
      namedExports: {
        'node_modules/react/react.js': ['Component', 'Children', 'createElement', 'PropTypes'],
        'node_modules/react-dom/server.js': ['renderToString']
      }
    }),
    globals(),
    replace({'process.env.NODE_ENV': JSON.stringify('development')}),
    resolve({
      jsnext: true,
      browser: true,
      main: true
    })
  ]
}
