class CreateIdentifiers < ActiveRecord::Migration
  def change
    create_table :identifiers do |t|

      t.timestamps
    end
  end
end
