require 'rails_helper'

RSpec.describe "Identifiers", :type => :request do
  describe "GET /identifiers" do
    it "works! (now write some real specs)" do
      get identifiers_path
      expect(response.status).to be(200)
    end
  end
end
