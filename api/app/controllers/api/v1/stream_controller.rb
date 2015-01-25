class Api::V1::StreamController < Api::V1::BaseController
  before_filter :authenticate_user!, :except => [:all]
  skip_authorization_check
  def all
    render json: Stream.all
  end
end
