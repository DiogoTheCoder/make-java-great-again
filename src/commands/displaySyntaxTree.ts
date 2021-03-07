import * as vscode from 'vscode';
import { getEditor } from '../utils';

export async function displaySyntaxTree(): Promise<void> {
  const editor = getEditor();

  const dotAst: string =
    (await vscode.commands.executeCommand(
      'mjga.langserver.generateDotAST',
      editor.document.uri.fsPath,
    )) ?? '';

  if (dotAst.length === 0) {
    vscode.window.showInformationMessage('Error!');
    return;
  }

  // Get the last part of the filename (after the last slash), e.g. Refactor.java
  const filename = editor.document.fileName.substring(
    editor.document.fileName.lastIndexOf('/') + 1,
  );

  const panel = vscode.window.createWebviewPanel(
    'syntaxTreeGraph',
    `${filename} - Simple Abstract Syntax Tree`,
    vscode.ViewColumn.One,
    {
      enableScripts: true,
    },
  );

  // And set its HTML content
  const html = getWebviewContent(dotAst);
  panel.webview.html = html;
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
