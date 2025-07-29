import { Plugin } from "prosemirror-state";
import { MenuView } from "./menuView";
import {
    toggleMark,
    setBlockType,
 } from "prosemirror-commands";

export const menuPlugin = (items) => {
  return new Plugin({
    view(editorView) {
      let menuView = new MenuView(items, editorView)
      editorView.dom.parentNode.insertBefore(menuView.dom, editorView.dom)
      return menuView
    }
  })
};

export const icon = (text, name) => {
  let span = document.createElement("span")
  span.className = "btn editor-menuicon " + name
  span.title = name
  span.textContent = text
  return span
};

export const removeMark = (markType) => {
  return function (state, dispatch) {
    const { from, to, empty } = state.selection;
    if (empty) return false;
    if (dispatch) dispatch(state.tr.removeMark(from, to, markType));
    return true;
  };
}

export const markActive = (state, type) => {
    const { from, $from, to, empty } = state.selection;
    if (empty) return !!type.isInSet(state.storedMarks || $from.marks());
    else return state.doc.rangeHasMark(from, to, type);
};

export const markButton = (markType, label) => {
    const dom = icon(label, markType.name);
    return {
        command: toggleMark(markType),
        dom,
        update: (view) => {
            const active = markActive(view.state, markType);
            dom.classList.toggle("active", active);
        }
    };
};

export const actionButton = (command, label) => {
  const dom = icon(label, command.name);
  return {
    command: command,
    dom
  }
};

export const historyButton = (command, label) => {
  const dom = icon(label, command.name || "history");
  return {
    command: command,
    dom,
    update: (view) => {
      // Check if the command can be executed
      const canExecute = command(view.state, null, view);
      dom.classList.toggle("inactive", !canExecute);
    }
  }
};

export const blockActive = (state, type, attrs = {}) => {
    const { $from, to, node } = state.selection;

    // For list nodes, check if we're inside a list of the specified type
    if (type.name === "bullet_list" || type.name === "ordered_list") {
        let depth = $from.depth;
        while (depth > 0) {
            const nodeAtDepth = $from.node(depth);
            if (nodeAtDepth.type === type) {
                return Object.keys(attrs).length === 0 || attrsMatch(nodeAtDepth.attrs, attrs);
            }
            depth--;
        }
        return false;
    }

    // For paragraph nodes, check if we're NOT inside a list
    if (type.name === "paragraph") {
        // Check if we're inside a list item
        let depth = $from.depth;
        while (depth > 0) {
            const nodeAtDepth = $from.node(depth);
            if (nodeAtDepth.type.name === "list_item") {
                return false; // We're inside a list, so paragraph button should not be active
            }
            depth--;
        }

        // We're not in a list, check if current node is a paragraph
        const targetNode = node || $from.parent;
        if (targetNode.type !== type) {
            return false;
        }

        return Object.keys(attrs).length === 0 || attrsMatch(targetNode.attrs, attrs);
    }

    // Get the actual node to check for other node types
    const targetNode = node || $from.parent;

    if (targetNode.type !== type) {
        return false;
    }

    // If no specific attrs are requested, just check the node type
    if (Object.keys(attrs).length === 0) {
        return true;
    }

    // Compare specific attributes
    return attrsMatch(targetNode.attrs, attrs);
};

const attrsMatch = (nodeAttrs, expectedAttrs) => {
    for (const key in expectedAttrs) {
        if (nodeAttrs[key] !== expectedAttrs[key]) {
            return false;
        }
    }
    return true;
};

export const blockButton = (nodeType, label, attrs = {}) => {
  const dom = icon(label, nodeType.name);

  return {
    command: (state, dispatch, view) => {
      const { $from } = state.selection;

      // Check if we're inside a list item
      let inListItem = false;
      let listItemDepth = -1;
      for (let d = $from.depth; d >= 0; d--) {
        if ($from.node(d).type.name === "list_item") {
          inListItem = true;
          listItemDepth = d;
          break;
        }
      }

      if (inListItem && nodeType.name === "paragraph") {
        // Paragraph button clicked while in list - convert list item to paragraph
        if (dispatch) {
          const listItemStart = $from.before(listItemDepth);
          const listItemEnd = $from.after(listItemDepth);
          const content = $from.node(listItemDepth).content;
          const paragraphNode = state.schema.nodes.paragraph.create(attrs, content);
          dispatch(state.tr.replaceWith(listItemStart, listItemEnd, paragraphNode));
          return true;
        }
        return false;
      }

      // Check if the current block is already of this type with these attrs
      const isActive = blockActive(state, nodeType, attrs);

      if (isActive && nodeType.name !== "paragraph") {
        // If active and not a paragraph button, toggle back to paragraph
        return setBlockType(state.doc.type.schema.nodes.paragraph)(state, dispatch, view);
      } else {
        // Otherwise, set to the requested block type
        return setBlockType(nodeType, attrs)(state, dispatch, view);
      }
    },
    dom,
    update: (view) => {
      const active = blockActive(view.state, nodeType, attrs);
      dom.classList.toggle("active", active);
    },
  };
};

export const listButton = (listType, label) => {
  const dom = icon(label, listType.name);

  // Custom list command that works with inline list items
  const createListCommand = (listNodeType) => {
    return (state, dispatch) => {
      const { selection, schema } = state;
      const { $from, $to } = selection;

      // Check if we're already in this type of list
      let inList = false;
      let listItemDepth = -1;

      for (let d = $from.depth; d >= 0; d--) {
        const node = $from.node(d);
        if (node.type === listNodeType) {
          inList = true;
          break;
        }
        if (node.type.name === "list_item" && listItemDepth === -1) {
          listItemDepth = d;
        }
      }

      if (inList) {
        // If we're in the list, convert list item back to paragraph
        if (listItemDepth !== -1 && dispatch) {
          const listItemStart = $from.before(listItemDepth);
          const listItemEnd = $from.after(listItemDepth);
          const content = $from.node(listItemDepth).content;
          const paragraphNode = schema.nodes.paragraph.create(null, content);
          dispatch(state.tr.replaceWith(listItemStart, listItemEnd, paragraphNode));
          return true;
        }
        return false;
      }

      // Get the range to wrap
      let range = $from.blockRange($to);
      if (!range) return false;

      // Create a list item containing the current content
      const listItem = schema.nodes.list_item;
      if (!listItem) return false;

      if (!dispatch) return true;

      // Get the content from the current paragraph
      const content = range.$from.parent.content;

      // Create list item with the inline content directly
      const listItemNode = listItem.create(null, content);
      const listNode = listNodeType.create(null, [listItemNode]);

      // Replace the current block with the list
      const tr = state.tr.replaceWith(range.start, range.end, listNode);
      dispatch(tr);
      return true;
    };
  };

  return {
    command: createListCommand(listType),
    dom,
    update: (view) => {
      const active = blockActive(view.state, listType);
      dom.classList.toggle("active", active);
    },
  };
};export const linkButton = (markType, label) => {
  const dom = icon(label, markType.name);

  const linkCommand = (state, dispatch) => {
    const { from, to, empty } = state.selection;

    if (empty) {
      // No selection - insert a new link
      const href = prompt("Enter URL:");
      if (!href) return false;

      const linkText = prompt("Enter link text:", href);
      if (!linkText) return false;

      if (dispatch) {
        const linkMark = markType.create({ href });
        const textNode = state.schema.text(linkText, [linkMark]);
        dispatch(state.tr.replaceSelectionWith(textNode, false));
      }
      return true;
    } else {
      // Text is selected
      const existingLink = markActive(state, markType);

      if (existingLink) {
        // Remove existing link
        if (dispatch) {
          dispatch(state.tr.removeMark(from, to, markType));
        }
        return true;
      } else {
        // Add link to selected text
        const href = prompt("Enter URL:");
        if (!href) return false;

        if (dispatch) {
          const linkMark = markType.create({ href });
          dispatch(state.tr.addMark(from, to, linkMark));
        }
        return true;
      }
    }
  };

  return {
    command: linkCommand,
    dom,
    update: (view) => {
      const active = markActive(view.state, markType);
      dom.classList.toggle("btn-outline", !active);
      dom.classList.toggle("btn-blank", active);
    }
  };
}



export const menuGroup = (items, className = "editor-menu-group") => {
  const container = document.createElement("div");
  container.className = className;

  return {
    command: () => {}, // Groups don't have commands
    dom: container,
    items: items, // Store items for the menu view to handle
    update: () => {}, // Groups don't need updates
    isGroup: true // Flag to identify groups
  };
};

export const dropdown = (label, options, className = "dropdown menu-dropdown") => {
  const container = document.createElement("div");
  container.className = className;
  container.style.position = "relative";
  container.style.display = "inline-block";

  const button = document.createElement("button");
  button.className = "btn btn-sm btn-light dropdown-toggle";
  button.textContent = label;
  button.type = "button";

  const menu = document.createElement("div");
  menu.className = "dropdown-menu";
  menu.style.display = "none";

  // Store references for updates
  const menuItems = [];

  for (const option of options) {
    const item = document.createElement("button");
    item.className = "dropdown-item";
    item.type = "button";
    item.textContent = option.label;

    // Store the option reference for active state checking
    item.option = option;
    menuItems.push(item);

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

  return {
    command: () => {}, // Dropdowns don't have direct commands
    dom: container,
    menuItems: menuItems, // Store for update access
    update: (view, editorView) => {
      // Update active states for all menu items
      menuItems.forEach(item => {
        const option = item.option;
        let isActive = false;

        if (option.isActive) {
          // Custom active check function
          isActive = option.isActive(view.state, editorView);
        } else if (option.activeCheck) {
          // Legacy: custom active check
          isActive = option.activeCheck(view.state);
        } else if (option.nodeType && option.attrs) {
          // Block node with attributes check
          isActive = blockActive(view.state, option.nodeType, option.attrs);
        } else if (option.nodeType) {
          // Simple block node check
          isActive = blockActive(view.state, option.nodeType);
        } else if (option.markType) {
          // Mark check
          isActive = markActive(view.state, option.markType);
        }

        // Apply classes based on active state
        const activeClass = option.activeClass || "active";
        const inactiveClass = option.inactiveClass || "";

        // Helper function to add/remove multiple classes
        const addClasses = (element, classString) => {
          if (classString) {
            const classes = classString.split(/\s+/).filter(cls => cls.length > 0);
            classes.forEach(cls => element.classList.add(cls));
          }
        };

        const removeClasses = (element, classString) => {
          if (classString) {
            const classes = classString.split(/\s+/).filter(cls => cls.length > 0);
            classes.forEach(cls => element.classList.remove(cls));
          }
        };

        if (isActive) {
          addClasses(item, activeClass);
          removeClasses(item, inactiveClass);
        } else {
          removeClasses(item, activeClass);
          addClasses(item, inactiveClass);
        }
      });
    },
    setupEventListeners: (editorView) => {
      // Setup click handlers with the specific editor view
      menuItems.forEach(item => {
        item.addEventListener("click", (e) => {
          e.preventDefault();
          const option = item.option;
          if (option.command) {
            option.command(editorView.state, editorView.dispatch, editorView);
          }
          menu.style.display = "none";
        });
      });
    },
    isDropdown: true // Flag to identify dropdowns
  };
};