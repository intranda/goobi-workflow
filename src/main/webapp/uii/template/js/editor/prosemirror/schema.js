// schema.js
import { Schema } from "prosemirror-model";

// Define all marks
const marks = {
  strong: {
    parseDOM: [{ tag: "strong" }, { tag: "b" }],
    toDOM: () => ["strong", 0],
  },
  em: {
    parseDOM: [{ tag: "em" }, { tag: "i" }],
    toDOM: () => ["em", 0],
  },
  underline: {
    parseDOM: [{ tag: "u" }, { style: "text-decoration=underline" }],
    toDOM: () => ["u", 0],
  },
  strike: {
    parseDOM: [{ tag: "s" }, { tag: "del" }, { style: "text-decoration=line-through" }],
    toDOM: () => ["s", 0],
  },
  superscript: {
    parseDOM: [{ tag: "sup" }],
    toDOM: () => ["sup", 0],
  },
  subscript: {
    parseDOM: [{ tag: "sub" }],
    toDOM: () => ["sub", 0],
  },
  code: {
    parseDOM: [{ tag: "code" }],
    toDOM: () => ["code", 0],
  },
  link: {
    attrs: {
      href: { default: null },
      title: { default: null }
    },
    inclusive: false,
    parseDOM: [{
      tag: "a[href]",
      getAttrs(dom) {
        return {
          href: dom.getAttribute("href"),
          title: dom.getAttribute("title")
        }
      }
    }],
    toDOM(node) {
      const { href, title } = node.attrs;
      const attrs = { href };
      if (title) attrs.title = title;
      return ["a", attrs, 0];
    }
  },
};

// Define all nodes
const nodes = {
  doc: {
    content: "block+"
  },

  paragraph: {
    content: "inline*",
    group: "block",
    attrs: {
      align: { default: null },
    },
    parseDOM: [
      {
        tag: "p",
        getAttrs: (node) => {
          const align = node.style?.textAlign || node.getAttribute("align");
          return { align: align || null };
        },
      },
    ],
    toDOM: (node) => {
      const { align } = node.attrs;
      const attrs = align ? { style: `text-align: ${align}` } : {};
      return ["p", attrs, 0];
    },
  },

  heading: {
    attrs: { level: { default: 1 } },
    content: "inline*",
    group: "block",
    defining: true,
    parseDOM: [
      { tag: "h1", attrs: { level: 1 } },
      { tag: "h2", attrs: { level: 2 } },
      { tag: "h3", attrs: { level: 3 } },
      { tag: "h4", attrs: { level: 4 } },
      { tag: "h5", attrs: { level: 5 } },
      { tag: "h6", attrs: { level: 6 } }
    ],
    toDOM(node) {
      return ["h" + node.attrs.level, 0];
    }
  },

  code_block: {
    content: "text*",
    marks: "",
    group: "block",
    code: true,
    defining: true,
    parseDOM: [{ tag: "pre", preserveWhitespace: "full" }],
    toDOM: () => ["pre", ["code", 0]],
  },

  text: {
    group: "inline"
  },

  hard_break: {
    inline: true,
    group: "inline",
    selectable: false,
    parseDOM: [{ tag: "br" }],
    toDOM: () => ["br"],
  },

  bullet_list: {
    content: "list_item+",
    group: "block",
    parseDOM: [{ tag: "ul" }],
    toDOM: () => ["ul", 0],
  },

  ordered_list: {
    attrs: { order: { default: 1 } },
    content: "list_item+",
    group: "block",
    parseDOM: [{
      tag: "ol",
      getAttrs(dom) {
        return { order: dom.hasAttribute("start") ? +dom.getAttribute("start") : 1 };
      }
    }],
    toDOM(node) {
      return node.attrs.order == 1 ? ["ol", 0] : ["ol", { start: node.attrs.order }, 0];
    }
  },

  list_item: {
    content: "inline*",
    parseDOM: [{ tag: "li" }],
    toDOM: () => ["li", 0],
    defining: true
  },
};

export const schema = new Schema({
  nodes,
  marks,
});