import { Directive, HostListener, Input } from "@angular/core";

@Directive({
  selector: "[appStrictInput]",
  standalone: true
})
export class StrictInputDirective {
  @Input() appStrictInput: "numeric" | "text" | "email" = "text";

  @HostListener("keydown", ["$event"])
  onKeyDown(event: KeyboardEvent) {
    // Permite teclas de controle (Backspace, Tab, Setas)
    if (
      event.key === "Backspace" || 
      event.key === "Tab" || 
      event.key === "ArrowLeft" || 
      event.key === "ArrowRight" ||
      event.key === "Delete"
    ) {
      return;
    }

    if (this.appStrictInput === "numeric") {
      // Impede digitação de não-números
      if (!/^[0-9]+$/.test(event.key)) {
        event.preventDefault();
      }
    }
  }
}
