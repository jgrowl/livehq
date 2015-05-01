import 'package:polymer/polymer.dart';
import 'package:observe/observe.dart';

@CustomTag('paper-toggle-icon-button')
  class PaperToggleIconButton extends PolymerElement {
  /**
   * Gets or sets the state, `true` is checked and `false` is unchecked.
   *
   * @attribute checked
   * @type boolean
   * @default false
   */
  @observable bool checked;

  /**
   * If true, the toggle button is disabled.  A disabled toggle button cannot
   * be tapped.
   *
   * @attribute disabled
   * @type boolean
   * @default false
   */
  var disabled = false;

  var activeIcon;
  var inactiveIcon;

  @ComputedProperty('checked ? activeIcon : inactiveIcon')
  String get icon => readValue(#icon);

  PaperToggleIconButton.created() : super.created();

  void toggleChecked() {
    checked = !checked;
  }
}