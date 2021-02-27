import * as mulang from 'mulang';
import * as path from 'path';
import * as vscode from 'vscode';
import { flatten, unflatten } from 'flat';
import { getEditor } from '../utils';

const _ = require('lodash');
require('deepdash')(_);

export function displaySyntaxTree() {
  const editor = getEditor();
  const code = editor.document.getText();

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

  let ast = {
    tag: '',
    contents: {},
  };

  try {
    ast = mulang.nativeCode('Java', code).ast;
  } catch (error) {
    throw Error(error.message);
  }

  ast = _.filterDeep(ast, (value: any, key: any, parent: any) => {
    if (value !== null) {
      if (Array.isArray(value) && value.length === 0) {
        return false;
      }

      return true;
    }

    return false;
  });

  // First 'contents' is the Class name
  ast.contents = [
    {
      tag: ast.contents[0],
      contents: [ast.contents[1]],
    },
  ];

  // Fix up the EntryPoint as well
  ast.contents[0].contents[0].contents = [
    {
      tag: ast.contents[0].contents[0].contents[0],
      contents: [ast.contents[0].contents[0].contents[1]],
    },
  ];

  let flattenAst = flatten(ast) as object;
  let flattedModifiedAst = {};
  Object.keys(flattenAst).forEach((key: string) => {
    let newKey = key;
    let newValue = flattenAst[key];

    newKey = newKey.replace(/tag/g, 'name');

    if (newKey.endsWith('contents')) {
      newValue = [newValue];
      newKey = newKey.concat('.0');
    }

    newKey = newKey.replace(/contents/g, 'children');
    if (flattenAst[key] === null) {
      newValue = '';
    }

    let lastChar = newKey.substr(newKey.length - 1);
    if (!isNaN(parseInt(lastChar))) {
      newKey = newKey.concat('.name');
    }

    flattedModifiedAst[newKey] = newValue;
  });

  let jsonString = JSON.stringify(unflatten(flattedModifiedAst));
  const newFile = vscode.Uri.parse(
    'untitled:' +
      path.join(
        vscode.workspace.workspaceFolders!.toString(),
        'simple-ast.json',
      ),
  );

  vscode.workspace.openTextDocument(newFile).then((document) => {
    const edit = new vscode.WorkspaceEdit();
    edit.insert(newFile, new vscode.Position(0, 0), jsonString);
    return vscode.workspace.applyEdit(edit).then((success) => {
      if (success) {
        vscode.window.showTextDocument(document);
      } else {
        vscode.window.showInformationMessage('Error!');
      }
    });
  });

  // And set its HTML content
  panel.webview.html = getWebviewContent(jsonString);
}

function getWebviewContent(treeData: string) {
  return `<!DOCTYPE html>
  <html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simple Abstract Syntax Tree</title>
    <style>
      .node {
        cursor: pointer;
      }
      
      .node circle {
        stroke-width: 3px;
      }
      
      .node text {
        font: 12px sans-serif;
        fill: #fff;
      }
      
      .link {
        fill: none;
        stroke: #ccc;
        stroke-width: 2px;
      }
      
      .tree {
        margin-bottom: 10px;
        overflow: auto;
      }
    </style>
  </head>
  <body>
    <h1>Simple Abstract Syntax Tree</div>
    <br />
    <div id="tree"></div>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.17/d3.min.js"></script>
    <script>
      var margin = { top: 40, right: 120, bottom: 20, left: 120 };
      var width = window.innerWidth / 2;
      var height = window.innerHeight;

      var i = 0, duration = 750;
      var tree = d3.layout.tree()
          .size([height, width]);
      var diagonal = d3.svg.diagonal()
          .projection(function (d) { return [d.x, d.y]; });
      var svg = d3.select("#tree").append("svg")
          .attr("width", width + margin.right + margin.left)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
      var root = ${treeData};

      update(root);
      
      function update(source) {
        // Compute the new tree layout.
        var nodes = tree.nodes(root).reverse(),
            links = tree.links(nodes);
        // Normalize for fixed-depth.
        nodes.forEach(function (d) { d.y = d.depth * 100; });
        // Declare the nodes…
        var node = svg.selectAll("g.node")
            .data(nodes, function (d) { return d.id || (d.id = ++i); });
        // Enter the nodes.
        var nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .attr("transform", function (d) {
                return "translate(" + source.x0 + "," + source.y0 + ")";
            }).on("click", nodeclick);
        nodeEnter.append("circle")
        .attr("r", 10)
            .attr("stroke", function (d)
            { return d.children || d._children ?
            "steelblue" : "#00c13f"; })
            .style("fill", function (d)
            { return d.children || d._children ?
            "lightsteelblue" : "#fff"; });
        //.attr("r", 10)
        //.style("fill", "#fff");
        nodeEnter.append("text")
            .attr("y", function (d) {
                return d.children || d._children ? -18 : 18;
            })
            .attr("dy", ".35em")
            .attr("text-anchor", "middle")
            .text(function (d) { return d.name; })
            .style("fill-opacity", 1e-6);
        // Transition nodes to their new position.
        //horizontal tree
        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function (d)
            { return "translate(" + d.x +
            "," + d.y + ")"; });
        nodeUpdate.select("circle")
            .attr("r", 10)
            .style("fill", function (d)
            { return d._children ? "lightsteelblue" : "#fff"; });
        nodeUpdate.select("text")
            .style("fill-opacity", 1);

        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function (d)
            { return "translate(" + source.x +
            "," + source.y + ")"; })
            .remove();
        nodeExit.select("circle")
            .attr("r", 1e-6);
        nodeExit.select("text")
            .style("fill-opacity", 1e-6);
        // Update the links…
        // Declare the links…
        var link = svg.selectAll("path.link")
            .data(links, function (d) { return d.target.id; });
        // Enter the links.
        link.enter().insert("path", "g")
            .attr("class", "link")

            .attr("d", function (d) {
                var o = { x: source.x0, y: source.y0 };
                return diagonal({ source: o, target: o });
            });
        // Transition links to their new position.
        link.transition()
            .duration(duration)
        .attr("d", diagonal);

        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function (d) {
                var o = { x: source.x, y: source.y };
                return diagonal({ source: o, target: o });
            })
            .remove();

        // Stash the old positions for transition.
        nodes.forEach(function (d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
      }

      // Toggle children on click.
      function nodeclick(d) {
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else {
            d.children = d._children;
            d._children = null;
        }
        update(d);
      }

    </script>
  </body>
  </html>`;
}
