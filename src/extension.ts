import { CstNode } from "chevrotain";
import * as path from "path";
import * as prettierJava from "prettier-plugin-java";
import * as vscode from "vscode";
import { TextEditor } from "vscode";
import { transformCode } from "./transformCode";
import { flatten, unflatten } from "flat";
import * as mulang from "mulang";

const _ = require("lodash");
require("deepdash")(_);

export function activate(context: vscode.ExtensionContext) {
  // Use the console to output diagnostic information (console.log) and errors (console.error)
  // This line of code will only be executed once when your extension is activated
  console.log("Let's Make Java Great Again!");

  context.subscriptions.push(
    vscode.commands.registerCommand(
      "make-java-great-again.refactorFile",
      () => {
        refactorFile();
      }
    ),
    vscode.commands.registerCommand(
      "make-java-great-again.displaySyntaxTree",
      () => {
        displaySyntaxTree();
      }
    )
  );
}

function refactorFile(): void {
  const code = readCode();
  if (typeof code !== "string") {
    throw Error("Was unable to read code!");
  }

  const cst = parse(code);
  const transformedCode = transformCode(cst);
  //writeCode(transformedCode);
}

function displaySyntaxTree() {
  console.log("test");
  const editor = getEditor();
  const code = editor.document.getText();
  if (typeof code !== "string") {
    throw Error("Was unable to read code!");
  }

  // Get the last part of the filename (after the last slash), e.g. Refactor.java
  const filename = editor.document.fileName.substring(
    editor.document.fileName.lastIndexOf("/") + 1
  );
  const panel = vscode.window.createWebviewPanel(
    "syntaxTreeGraph",
    `${filename} - Simple Abstract Syntax Tree`,
    vscode.ViewColumn.One,
    {
      enableScripts: true,
    }
  );

  let ast = {
    tag: "",
    contents: {},
  };

  try {
    ast = mulang.nativeCode("Java", code).ast;
  } catch (error) {
    throw Error(error.message);
  }

  ast = _.filterDeep(ast, (value, key, parent) => {
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

    newKey = newKey.replace(/tag/g, "name");

    if (newKey.endsWith("contents")) {
      newValue = [newValue];
      newKey = newKey.concat(".0");
    }

    newKey = newKey.replace(/contents/g, "children");
    if (flattenAst[key] === null) {
      newValue = "";
    }

    let lastChar = newKey.substr(newKey.length - 1);
    if (!isNaN(parseInt(lastChar))) {
      newKey = newKey.concat(".name");
    }

    flattedModifiedAst[newKey] = newValue;
  });

  let jsonString = JSON.stringify(unflatten(flattedModifiedAst));
  const newFile = vscode.Uri.parse(
    "untitled:" +
      path.join(
        vscode.workspace.workspaceFolders!.toString(),
        "simple-ast.json"
      )
  );

  vscode.workspace.openTextDocument(newFile).then((document) => {
    const edit = new vscode.WorkspaceEdit();
    edit.insert(newFile, new vscode.Position(0, 0), jsonString);
    return vscode.workspace.applyEdit(edit).then((success) => {
      if (success) {
        vscode.window.showTextDocument(document);
      } else {
        vscode.window.showInformationMessage("Error!");
      }
    });
  });

  // And set its HTML content
  panel.webview.html = getWebviewContent(jsonString);
}

function parse(code: string): CstNode {
  return prettierJava.parsers.java.parse(code, null, "ordinaryCompilationUnit");
}

function readCode(): string {
  const editor = getEditor();
  return editor.document.getText();
}

function writeCode(code: string): void {
  const editor = getEditor();
  const edit = new vscode.WorkspaceEdit();

  const wholeDocument = new vscode.Range(
    new vscode.Position(0, 0),
    new vscode.Position(editor.document.lineCount, 0)
  );

  const updateCode = new vscode.TextEdit(wholeDocument, code);
  edit.set(editor.document.uri, [updateCode]);

  vscode.workspace.applyEdit(edit);
}

function getEditor(): TextEditor {
  const editor = vscode.window.activeTextEditor;
  if (!editor) {
    throw Error("No active editor!");
  }

  return editor;
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

// this method is called when your extension is deactivated
export function deactivate() {}
