import * as vscode from 'vscode';
import { getActiveDocumentFilePath, getEditor } from '../utils';

export async function displaySyntaxTree(): Promise<vscode.WebviewPanel> {
  const editor = getEditor();

  const dotAst: string =
    (await vscode.commands.executeCommand(
      'mjga.langserver.generateDotAST',
      editor.document.uri.fsPath,
    )) ?? '';

  if (dotAst.length === 0) {
    throw new Error('Unable to generate AST');
  }

  const panel = vscode.window.createWebviewPanel(
    'syntaxTreeGraph',
    `${getActiveDocumentFilePath()} - Simple Abstract Syntax Tree`,
    vscode.ViewColumn.One,
    {
      enableScripts: true,
    },
  );

  // And set its HTML content
  const html = getWebviewContent(dotAst);
  panel.webview.html = html;

  return panel;
}

function getWebviewContent(treeData: string) {
  return `
  <!DOCTYPE html>
  <meta charset="utf-8">
  <body>
    <script src="https://d3js.org/d3.v5.min.js"></script>
    <script src="https://unpkg.com/@hpcc-js/wasm@0.3.11/dist/index.min.js"></script>
    <script src="https://unpkg.com/d3-graphviz@3.0.5/build/d3-graphviz.js"></script>
    <h1>Simple Abstract Syntax Tree</h1>
    <div id="graph" style="text-align: center;"></div>
    <script>
      // to avoid scrollbars
      var margin = 20;
      
      var graphviz = d3.select("#graph").graphviz()
      .attributer(attributer)
          .logEvents(true)
          .on("initEnd", render);
      
          function attributer(datum, index, nodes) {
          var selection = d3.select(this);
          if (datum.tag == "svg") {
              var width = window.innerWidth;
              var height = window.innerHeight;
              selection
                  .attr("width", width)
                  .attr("height", height)
              datum.attributes.width = width - margin;
              datum.attributes.height = height - margin;
          }
      }
      
      function render() {
          graphviz
          .renderDot(\`${treeData}\`)
          .zoom(true);
      }
      
      function attributer(datum, index, nodes) {
          var selection = d3.select(this);
          if (datum.tag == "svg") {
              var width = window.innerWidth;
              var height = window.innerHeight;
              selection
                  .attr("width", width)
                  .attr("height", height)
              datum.attributes.width = width - margin;
              datum.attributes.height = height - margin;
          }
      }
      
      function resetZoom() {
          graphviz
              .resetZoom(d3.transition().duration(1000));
      }
      
      function resizeSVG() {
          console.log('Resize');
          var width = window.innerWidth;
          var height = window.innerHeight;
          d3.select("#graph").selectWithoutDataPropagation("svg")
              .transition()
              .duration(700)
              .attr("width", width - margin)
              .attr("height", height - margin);
      };
      
      d3.select(window).on("resize", resizeSVG);
      d3.select(window).on("click", resetZoom);
    </script>
    <body>
  `;
}
