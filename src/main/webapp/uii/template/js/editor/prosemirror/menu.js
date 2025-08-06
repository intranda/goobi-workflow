// menu.js
import {
    setBlockType,
    chainCommands,
 } from "prosemirror-commands";
import { undo, redo } from "prosemirror-history";
import { DOMSerializer } from "prosemirror-model";

import { removeMark } from "./menuPlugin";
import {
  menuPlugin,
  icon,
  markButton,
  blockButton,
  listButton,
  historyButton,
  linkButton,
  menuGroup,
  dropdown,
} from "./menuPlugin";
import { schema } from "./schema";

const heading = (level) => {
    return blockButton(schema.nodes.heading, `H${level}`, {level});
};

const bold = markButton(schema.marks.strong, "B");

const emph = markButton(schema.marks.em, "I");

const paragraph = blockButton(schema.nodes.paragraph, "P");

const underline = markButton(schema.marks.underline, "U");

const strike = markButton(schema.marks.strike, "S");

const superscript = markButton(schema.marks.superscript, "x²");

const subscript = markButton(schema.marks.subscript, "x₂");

const link = linkButton(schema.marks.link, "🔗");

const codeBlock = blockButton(schema.nodes.code_block, "{}");

const bulletList = listButton(schema.nodes.bullet_list, "•");

const orderedList = listButton(schema.nodes.ordered_list, "1.");

const undoButton = historyButton(undo, "↩");

const redoButton = historyButton(redo, "↪");

const clearFormatting = {
  command: chainCommands(
    ...Object.keys(schema.marks).map((mark) =>
      removeMark(schema.marks[mark])
    )
  ),
  dom: icon("✖", "clear-format"),
};

const copySource = {
  command: (state, dispatch, view) => {
    // Serialize the current document to HTML
    const serializer = DOMSerializer.fromSchema(schema);
    const fragment = serializer.serializeFragment(state.doc.content);
    const div = document.createElement("div");
    div.appendChild(fragment);
    const html = div.innerHTML;

    // Format HTML with proper indentation
    const formatHTML = (html) => {
      const tab = '    ';
      let result = '';
      let indent = '';

      // Split by tag boundaries but keep the angle brackets
      const parts = html.split(/(<[^>]*>)/);

      for (let i = 0; i < parts.length; i++) {
        const part = parts[i];

        if (part.match(/^<[^>]*>$/)) {
          // This is a tag
          if (part.match(/^<\/\w/)) {
            // Closing tag - decrease indent before adding
            indent = indent.substring(tab.length);
          }

          result += indent + part + '\n';

          if (part.match(/^<\w[^>]*[^\/]>$/) && !part.match(/^<(area|base|br|col|embed|hr|img|input|link|meta|param|source|track|wbr)/)) {
            // Opening tag (not self-closing) - increase indent after adding
            indent += tab;
          }
        } else if (part.trim()) {
          // This is text content (only add if not empty)
          result += indent + part.trim() + '\n';
        }
      }

      return result.trim();
    };

    const formattedHTML = formatHTML(html);

    // Copy to clipboard
    if (navigator.clipboard && window.isSecureContext) {
      // Use modern Clipboard API
      navigator.clipboard.writeText(formattedHTML).then(() => {
        console.log("Formatted HTML copied to clipboard");
      }).catch(err => {
        console.error("Failed to copy to clipboard:", err);
      });
    } else {
      // Fallback for older browsers
      const textArea = document.createElement("textarea");
      textArea.value = formattedHTML;
      textArea.style.position = "fixed";
      textArea.style.left = "-999999px";
      textArea.style.top = "-999999px";
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      try {
        document.execCommand('copy');
        console.log("Formatted HTML copied to clipboard (fallback)");
      } catch (err) {
        console.error("Failed to copy to clipboard:", err);
      }
      document.body.removeChild(textArea);
    }
    return true;
  },
  dom: icon("📋", "copy-source"),
};

// --- Text alignment buttons ---
const align = (value) => ({
  command: setBlockType(schema.nodes.paragraph, { align: value }),
  dom: icon(value[0].toUpperCase(), `align-${value}`),
});

// --- Format dropdown menu (headings + alignment) ---
const createFormatDropdown = (editorView) => ({
    command: () => {},
  dom: (() => {
    const container = document.createElement("div");
    container.className = "dropdown menu-dropdown";
    container.style.position = "relative";
    container.style.display = "inline-block";

    const button = document.createElement("button");
    button.className = "btn btn-sm btn-light dropdown-toggle";
    button.textContent = "Format";
    button.type = "button";

    const menu = document.createElement("div");
    menu.className = "dropdown-menu show"; // use "show" to keep it always open for simplicity
    menu.style.display = "none"; // control via JS

    const options = [
      { label: "Normal", command: setBlockType(schema.nodes.paragraph) },
      { label: "Heading 1", command: setBlockType(schema.nodes.heading, { level: 1 }) },
      { label: "Heading 2", command: setBlockType(schema.nodes.heading, { level: 2 }) },
      { label: "Heading 3", command: setBlockType(schema.nodes.heading, { level: 3 }) },
      { label: "Align Left", command: setBlockType(schema.nodes.paragraph, { align: "left" }) },
      { label: "Align Center", command: setBlockType(schema.nodes.paragraph, { align: "center" }) },
      { label: "Align Right", command: setBlockType(schema.nodes.paragraph, { align: "right" }) },
    ];

    for (const { label, command } of options) {
      const item = document.createElement("button");
      item.className = "dropdown-item";
      item.type = "button";
      item.textContent = label;
      item.addEventListener("click", (e) => {
        e.preventDefault();
        const view = editorView || window.view; // Fallback to global for legacy support
        command(view.state, view.dispatch, view);
        menu.style.display = "none";
      });
      menu.appendChild(item);
    }

    button.addEventListener("click", (e) => {
      e.preventDefault();
      menu.style.display = menu.style.display === "none" ? "block" : "none";
    });

    // Close dropdown if clicked outside
    document.addEventListener("click", (e) => {
      if (!container.contains(e.target)) {
        menu.style.display = "none";
      }
    });

    container.appendChild(button);
    container.appendChild(menu);

    return container;
  })(),
  update: () => {},
});

export const createMenu = (editorView) => {
    // Create format dropdown with active state detection
    const formatDropdown = dropdown("Format", [
      {
        label: "Normal",
        command: setBlockType(schema.nodes.paragraph),
        nodeType: schema.nodes.paragraph,
        activeClass: "active",
        inactiveClass: "text-muted"
      },
      {
        label: "Heading 1",
        command: setBlockType(schema.nodes.heading, { level: 1 }),
        nodeType: schema.nodes.heading,
        attrs: { level: 1 },
        activeClass: "active font-weight-bold",
        inactiveClass: "text-muted"
      },
      {
        label: "Heading 2",
        command: setBlockType(schema.nodes.heading, { level: 2 }),
        nodeType: schema.nodes.heading,
        attrs: { level: 2 },
        activeClass: "active font-weight-bold",
        inactiveClass: "text-muted"
      },
      {
        label: "Heading 3",
        command: setBlockType(schema.nodes.heading, { level: 3 }),
        nodeType: schema.nodes.heading,
        attrs: { level: 3 },
        activeClass: "active font-weight-bold",
        inactiveClass: "text-muted"
      },
      {
        label: "Align Left",
        command: setBlockType(schema.nodes.paragraph, { align: "left" }),
        nodeType: schema.nodes.paragraph,
        attrs: { align: "left" },
        activeClass: "active text-primary",
        inactiveClass: "text-muted"
      },
      {
        label: "Align Center",
        command: setBlockType(schema.nodes.paragraph, { align: "center" }),
        nodeType: schema.nodes.paragraph,
        attrs: { align: "center" },
        activeClass: "active text-primary",
        inactiveClass: "text-muted"
      },
      {
        label: "Align Right",
        command: setBlockType(schema.nodes.paragraph, { align: "right" }),
        nodeType: schema.nodes.paragraph,
        attrs: { align: "right" },
        activeClass: "active text-primary",
        inactiveClass: "text-muted"
      },
    ]);

    return menuPlugin([
        // Text formatting group
        menuGroup([
            bold,
            emph,
            underline,
            strike,
            link,
        ]),

        // Script formatting group
        menuGroup([
            superscript,
            subscript,
        ]),

        // Block formatting group
        menuGroup([
            paragraph,
            heading(1), heading(2), heading(3), heading(4), heading(5), heading(6),
            codeBlock,
        ]),

        // List group
        menuGroup([
            bulletList,
            orderedList,
        ]),

        // Actions group
        menuGroup([
            copySource,
        ]),

        // History group
        menuGroup([
            undoButton,
            redoButton,
        ]),
    ]);
};