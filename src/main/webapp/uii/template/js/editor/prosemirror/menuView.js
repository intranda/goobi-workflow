export class MenuView {
  constructor(items, editorView) {
    this.items = []
    this.dropdowns = []
    this.editorView = editorView

    this.dom = document.createElement("div")
    this.dom.className = "editor-menubar"

    // Process items, handling groups and dropdowns
    items.forEach((item) => {
      if (item.isGroup) {
        // Add group container
        this.dom.appendChild(item.dom)
        // Add group items to the container and track them
        item.items.forEach((groupItem) => {
          item.dom.appendChild(groupItem.dom)
          this.items.push(groupItem)
          if (groupItem.isDropdown) {
            this.dropdowns.push(groupItem)
            groupItem.setupEventListeners(editorView)
          }
        })
      } else {
        // Regular item or dropdown
        this.dom.appendChild(item.dom)
        this.items.push(item)
        if (item.isDropdown) {
          this.dropdowns.push(item)
          item.setupEventListeners(editorView)
        }
      }
    })

    this.update()

    this.dom.addEventListener("mousedown", e => {
      e.preventDefault()
      editorView.focus()
      this.items.forEach(({command, dom}) => {
        if (dom.contains(e.target) && !e.target.closest('.dropdown-menu'))
          command(editorView.state, editorView.dispatch, editorView)
      })
    })
  }

  update() {
    this.items.forEach((item) => {
      if (typeof item.update === "function") {
        item.update(this.editorView, this.editorView);
      }
    });
  }

  destroy() { this.dom.remove() }
}